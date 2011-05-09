package org.msgpack.hadoop.mapred;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.io.LongWritable;

import org.msgpack.MessagePack;
import org.msgpack.Unpacker;
import org.msgpack.MessagePackObject;
import org.msgpack.hadoop.io.MessagePackWritable;

public class MessagePackRecordReader implements RecordReader<LongWritable, MessagePackWritable> {
    private Unpacker unpacker_;

    protected long start_;
    protected long pos_;
    protected long end_;
    private FSDataInputStream fileIn_;

    public MessagePackRecordReader(InputSplit genericSplit, JobConf conf) throws IOException {
        FileSplit split = (FileSplit)genericSplit;
        final Path file = split.getPath();

        // Open the file
        FileSystem fs = file.getFileSystem(conf);
        fileIn_ = fs.open(split.getPath());

        // Create streaming unpacker
        unpacker_ = new Unpacker(fileIn_);

        // Seek to the start of the split
        start_ = split.getStart();
        end_ = start_ + split.getLength();
        pos_ = start_;
    }

    public float getProgress() {
        if (start_ == end_) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos_ - start_) / (float) (end_ - start_));
        }
    }

    public long getPos() {
        return pos_;
    }

    public synchronized void close() throws IOException {
    }

    public LongWritable createKey() {
        return new LongWritable();
    }

    public MessagePackWritable createValue() {
        return new MessagePackWritable();
    }

    public boolean next(LongWritable key, MessagePackWritable val)
    throws IOException  {
        for (MessagePackObject obj : unpacker_) {
            key.set(fileIn_.getPos());
            val.set(obj);
            return true;
        }
        return false;
    }
}
