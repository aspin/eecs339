package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    private int groupByField;
    private Type groupByFieldType;
    private String groupByFieldName;
    private int aggregateField;
    private Op op;

    private HashMap<Field, Integer> groups; // see IntegerAggregator.java for comments

    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        if (what != Op.COUNT) {
            throw new IllegalArgumentException();
        }

        this.groupByField = gbfield;
        this.groupByFieldType = gbfieldtype;
        this.aggregateField = afield;
        this.op = what;
        this.groups = new HashMap<Field, Integer>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        if (this.groupByFieldName == null) {
            this.groupByFieldName = tup.getTupleDesc().getFieldName(this.groupByField);
        }

        Field key;
        if (this.groupByField != NO_GROUPING) {
            key = tup.getField(this.groupByField);
        } else {
            key = new IntField(-1);
        }

        if (this.groups.containsKey(key)) {
            this.groups.put(key, this.groups.get(key) + 1);
        } else {
            this.groups.put(key, 1);
        }
    }
    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    public DbIterator iterator() {
        ArrayList<Tuple> aggregateTuples = new ArrayList<Tuple>();

        // Construct descriptor
        Type[] types;
        String[] fields;
        if (this.groupByField == NO_GROUPING) {
            types = new Type[1];
            fields = new String[1];
            types[0] = Type.INT_TYPE;
            fields[0] = this.op.toString();
        } else {
            types = new Type[2];
            fields = new String[2];
            types[0] = this.groupByFieldType;
            fields[0] = this.groupByFieldName;
            types[1] = Type.INT_TYPE;
            fields[1] = this.op.toString();
        }
        TupleDesc tupleDesc = new TupleDesc(types, fields);

        for(Map.Entry<Field, Integer> entry : this.groups.entrySet()) {
            Field key = entry.getKey();
            Integer value = entry.getValue();
            Tuple tuple = new Tuple(tupleDesc);

            if (this.groupByField == NO_GROUPING) {
                tuple.setField(0, new IntField(value));
            } else {
                tuple.setField(0, key);
                tuple.setField(1, new IntField(value));
            }
            aggregateTuples.add(tuple);
        }
        return new TupleIterator(tupleDesc, aggregateTuples);
    }

}
