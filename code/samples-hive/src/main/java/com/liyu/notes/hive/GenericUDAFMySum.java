package com.liyu.notes.hive;

import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;

public class GenericUDAFMySum extends GenericUDAFEvaluator {

    @Override
    public AggregationBuffer getNewAggregationBuffer() throws HiveException {
        return null;
    }

    @Override
    public void reset(AggregationBuffer agg) throws HiveException {

    }

    @Override
    public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {

    }

    @Override
    public Object terminatePartial(AggregationBuffer agg) throws HiveException {
        return null;
    }

    @Override
    public void merge(AggregationBuffer agg, Object partial) throws HiveException {

    }

    @Override
    public Object terminate(AggregationBuffer agg) throws HiveException {
        return null;
    }
}
