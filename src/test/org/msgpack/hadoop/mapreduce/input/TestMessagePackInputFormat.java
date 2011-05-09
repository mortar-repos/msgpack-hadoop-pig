package org.msgpack.hadoop.mapreduce.input;

import java.io.*;
import java.util.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;

import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.mapreduce.*;

import org.msgpack.MessagePack;
import org.msgpack.MessagePackObject;
import org.msgpack.hadoop.io.MessagePackWritable;

import junit.framework.TestCase;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for MessagePackInputFormat.
 */
public class TestMessagePackInputFormat extends TestCase {
    private static final int MAX_LENGTH = 200;

    private static Configuration defaultConf = new Configuration();
    private static FileSystem localFs = null; 

    private static Path workDir = 
        new Path(new Path(System.getProperty("test.build.data", "."), "data"),
                 "TestLineInputFormat");

    public void testFormat() throws Exception {
        localFs = FileSystem.getLocal(defaultConf);
        localFs.delete(workDir, true);

        Job job = new Job(new Configuration(defaultConf));
        Path file = new Path(workDir, "test.txt");

        int seed = new Random().nextInt();
        Random random = new Random(seed);

        // for a variety of lengths
        for (int length = 0; length < MAX_LENGTH;
             length += random.nextInt(MAX_LENGTH/10) + 1) {
            // create a file with length entries
            BufferedOutputStream writer = new BufferedOutputStream(localFs.create(file));
            try {
                for (int i = 0; i < length; i++) {
                    long val = i;
                    byte[] raw = MessagePack.pack(val);
                    writer.write(raw, 0, raw.length);
                }
            } finally {
                writer.close();
            }
            checkFormat(job);
        }
    }

    void checkFormat(Job job) throws Exception {
        TaskAttemptContext attemptContext = new TaskAttemptContext(job.getConfiguration(),
                                                                   new TaskAttemptID("123", 0, false, 1, 2));

        MessagePackInputFormat format = new MessagePackInputFormat();
        FileInputFormat.setInputPaths(job, workDir);

        List<InputSplit> splits = format.getSplits(job);
        assertEquals(1, splits.size());
        for (int j = 0; j < splits.size(); j++) {
            RecordReader<LongWritable, MessagePackWritable> reader =
                format.createRecordReader(splits.get(j), attemptContext);
            reader.initialize(splits.get(j), attemptContext);

            int count = 0;
            try {
                while (reader.nextKeyValue()) {
                    LongWritable key = reader.getCurrentKey();
                    MessagePackWritable val = reader.getCurrentValue();
                    MessagePackObject obj = val.get();
                    assertEquals(count, obj.asLong());
                    count++;
                }
            } finally {
                reader.close();
            }
        }
    }
}
