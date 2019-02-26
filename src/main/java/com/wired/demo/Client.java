package com.wired.demo;


import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.EntryEventType;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.datamodel.TimestampedEntry;
import com.hazelcast.jet.function.DistributedPredicate;
import com.hazelcast.jet.pipeline.*;
import com.hazelcast.map.journal.EventJournalMapEvent;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.jet.Util.mapEventNewValue;
import static com.hazelcast.jet.Util.mapPutEvents;
import static com.hazelcast.jet.function.DistributedFunction.identity;
import static com.hazelcast.jet.pipeline.SinkBuilder.sinkBuilder;


public class Client extends Thread {


    private static final String SINK_NAME = "sink";

    private static final int SLIDING_WINDOW_LENGTH_MILLIS = 3_000;

    private static final int SLIDE_STEP_MILLIS = 100;

    private static final long WINDOW_SIZE = 15_000;

    private static final long SLIDING_STEP = 3_000;

    private static final String DISTINCT_MAP = "distinct";

    private static final String DISTINCT_ATTR_LIST = "distinct";

    /**
     * A GUI Sink which will show the results.
     */
    private static Sink<TimestampedEntry<String, Car>> buildCustomSink() {
        return sinkBuilder("Sink Custom Demo", instance -> {
            System.out.println("Initialize");
            return "Result: ";

        }).<TimestampedEntry<String, Car>>receiveFn((id, tsItem) -> {
            System.out.println(id + " " + tsItem);
        })
                .build();
    }

    /**
     * A GUI Sink which will show the frames with the maximum classification scores.
     */
    private static Sink<Set<String>> buildSetSink() {
        return sinkBuilder("Sink Custom Demo", instance -> {
            System.out.println("Initialize");
            return "ciao";

        })
                .<Set<String>>receiveFn((id, tsItem) -> {
                    System.out.println(id + " " + tsItem);
                })
                .build();
    }

    public static <K, V> DistributedPredicate<EventJournalMapEvent<K, V>> mapCustomPutEvents() {
        // return (e) -> { return e.getType() == EntryEventType.ADDED || e.getType() == EntryEventType.UPDATED || !e.getNewValue().equals(e.getOldValue());};
        return (e) -> {
            boolean eventValid = e.getType() == EntryEventType.ADDED || e.getType() == EntryEventType.UPDATED || !e.getNewValue().equals(e.getOldValue());
            if (eventValid) {
                Map<String, Serializable> changes = ((Car) e.getNewValue()).difference((Car) e.getOldValue());
                ((Car) e.getNewValue()).setResetValues(changes);
            }
            return eventValid;
        };
        // return e -> e.getType() == EntryEventType.UPDATED;
    }

    public void run() {
        System.out.println("Client running");

        try {
            ClientConfig clientConfig = new ClientConfig();
            GroupConfig groupConfig = new GroupConfig();
            clientConfig.getNetworkConfig().addAddress("localhost:5701");
            clientConfig.setGroupConfig(groupConfig);

            JetInstance localJet = Jet.newJetClient(clientConfig);


            Pipeline pipeline = Pipeline.create();
            StageWithWindow<Car> slidingWindow = pipeline.drawFrom(Sources.<Car, String, Car>mapJournal(Constants.MAP_NAME, mapCustomPutEvents(), mapEventNewValue(), JournalInitialPosition.START_FROM_CURRENT))
                    .addTimestamps(Car::getTime, TimeUnit.SECONDS.toMillis(0))
                    .window(WindowDefinition.tumbling(SLIDING_WINDOW_LENGTH_MILLIS));

            /**
             * Merge events happened on the same cars
             */
            StreamStage<TimestampedEntry<String, Car>> eventsHappened = slidingWindow.groupingKey(Car::getCarCode).aggregate(AggregateOperations.reducing(new Car(),
                    identity(), (d1, d2) -> new Car(d2.getCarCode(), d2.getTime()).mergeValues(d1).mergeValues(d2),
                    (d1, d2) -> d1)).setName("one aggregation");


            /*
             * Aggregate the events happened by code (car plate)
             */
            StreamStage<Set<String>> afterRollingAggregate = eventsHappened
                    .flatMap(e -> Traversers.traverseIterable(e.getValue().getValues().keySet()))
                    .rollingAggregate(AggregateOperations.toSet());

            /*
             * Put the events on a distinc map, to use it later
             */
            afterRollingAggregate.drainTo(Sinks.mapWithMerging(DISTINCT_MAP, strings -> DISTINCT_ATTR_LIST, identity(), (t1, t2) -> {
                t2.addAll(t1);
                return t2;
            }));
            /**
             * Show the events happened by code (car plate)
             */
            eventsHappened.drainTo(buildCustomSink());


            localJet.newJob(pipeline);

            /*
             * Pipeline with source from distinct map
             */
            Pipeline distinctMapPipeline = Pipeline.create();
            /*
             * We take all the attribute changed independently by the code of the car
             *
             * This is use to count which attributes will change, by time.
             */
            distinctMapPipeline.drawFrom(Sources.<Set<String>, String, Set<String>>mapJournal(DISTINCT_MAP, mapPutEvents(), mapEventNewValue(), JournalInitialPosition.START_FROM_CURRENT))
                    .addTimestamps()
                    .window(WindowDefinition.tumbling(SLIDING_WINDOW_LENGTH_MILLIS)).streamStage()
                    .drainTo(Sinks.logger());

            localJet.newJob(distinctMapPipeline);


            while (true) {
                TimeUnit.SECONDS.sleep(10);
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } finally {
            Hazelcast.shutdownAll();
            Jet.shutdownAll();
        }
    }


}



