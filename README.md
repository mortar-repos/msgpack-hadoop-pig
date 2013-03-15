MessagePack-Hadoop Integration
==============================

This package contains the bridge layer between MessagePack (http://msgpack.org)
and Hadoop (http://hadoop.apache.org/) families.

This enables you to run MR jobs on the MessagePack-formatted data, and also
enables you to load MessagePack data with Apache Pig.

Example Pig Usage
=================

Load [1, "aardvark"] or {"i": 1, "s": "aardvark"} ==> (1,aardvark)

    data = LOAD 'my_data.mpack' 
           USING org.msgpack.hadoop.pig.MessagePackLoader('i: int, s: chararray');

Load [1, "aardvark"] ==> {(1), (aardvark)}

    data = LOAD 'my_data.mpack' 
           USING org.msgpack.hadoop.pig.MessagePackLoader('b: {t: (b: bytearray)}');

Load {"i": 1, "s": "aardvark"} ==> {[i#1,s#aardvark]}

    data = LOAD 'my_data.mpack' 
           USING org.msgpack.hadoop.pig.MessagePackLoader('m: [bytearray]');
    -- or  USING org.msgpack.hadoop.pig.MessagePackLoader()
    -- to infer schema, including nested schemas

Load {"i": 1, "a": [1,2,3]} ==> (1,{(1),(2),(3)})

    data = LOAD 'my_data.mpack' 
           USING org.msgpack.hadoop.pig.MessagePackLoader('i: int, a: {t: (j: int)}');
