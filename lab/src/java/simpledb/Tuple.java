package simpledb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;

    private RecordId recordId;
    private Field fields[];
    private TupleDesc td;

    /**
     * Create a new tuple with the specified schema (type).
     *
     * @param td
     *            the schema of this tuple. It must be a__ valid TupleDesc
     *            instance with at least one field.
     */
    public Tuple(TupleDesc td) {
        this.td = td;
        this.fields = new Field[td.numFields()];
    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        return this.td;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        return this.recordId;
    }

    /**
     * Set the RecordId information for this tuple.
     *
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        this.recordId = rid;
    }

    /**
     * Change the value of the ith field of this tuple.
     *
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        this.fields[i] = f;
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     *
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        return this.fields[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     *
     * column1\tcolumn2\tcolumn3\t...\tcolumnN\n
     *
     * where \t is any whitespace, except newline, and \n is a newline
     */
    public String toString() {
        // TODO: write test case for this
        String stringRep = "";
        for(Field field : this.fields) {
           stringRep += field.toString() + " ";
        }
        stringRep += "\n";
        return stringRep;
    }

    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields() {
        return Arrays.asList(this.fields).iterator();
    }

    /**
     * reset the TupleDesc of thi tuple
     * */
    public void resetTupleDesc(TupleDesc td) {
        this.td = null;
    }

    public static Tuple merge(Tuple tuple1, Tuple tuple2) {
        TupleDesc mergedTd = TupleDesc.merge(tuple1.getTupleDesc(), tuple2.getTupleDesc());
        Tuple mergedTuple = new Tuple(mergedTd);
        int tuple1Fields = tuple1.getTupleDesc().numFields();
        for(int i = 0; i < mergedTd.numFields(); i++) {
            if (i < tuple1Fields) {
                mergedTuple.setField(i, tuple1.getField(i));
            } else {
                mergedTuple.setField(i, tuple2.getField(i - tuple1Fields));
            }
        }
        return mergedTuple;
    }
}
