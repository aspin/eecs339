package simpledb;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;

        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }

        // Adding this method for equality test
        public boolean equals(Object o) {
            boolean equality = false;
            if (o instanceof TDItem) {
                TDItem compare = (TDItem) o;
                equality = this.fieldType.equals(compare.fieldType);
            }
            return equality;
        }
    }

    private TDItem items[];

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        return Arrays.asList(this.items).iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        this.items = new TDItem[typeAr.length];
        for(int i = 0; i < typeAr.length; i++) {
            this.items[i] = new TDItem(typeAr[i], fieldAr[i]);
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        this(typeAr, new String[typeAr.length]);
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return this.items.length;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // TODO: manually throw exception? ensure test coverage
        return this.items[i].fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        return this.items[i].fieldType;
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        for(int i = 0; i < this.items.length; i++) {
            if (this.items[i].fieldName != null && this.items[i].fieldName.equals(name)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        int size = 0;
        for(TDItem item: this.items) {
            size += 4; // TODO: determine if this needs fixing
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // TODO: clean me up
        int totalFields = td1.numFields() + td2.numFields();
        Type mergedTypes[] = new Type[totalFields];
        String mergedNames[] = new String[totalFields];

        int i = 0;
        Iterator<TDItem> td1Iterator = td1.iterator();
        Iterator<TDItem> td2Iterator = td2.iterator();
        while (td1Iterator.hasNext()) {
            TDItem item = td1Iterator.next();
            mergedTypes[i] = item.fieldType;
            mergedNames[i] = item.fieldName;
            i += 1;
        }
        while (td2Iterator.hasNext()) {
            TDItem item = td2Iterator.next();
            mergedTypes[i] = item.fieldType;
            mergedNames[i] = item.fieldName;
            i++;
        }

        TupleDesc mergedTd = new TupleDesc(mergedTypes, mergedNames);
        return mergedTd;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     *
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        boolean equality = false;
        if (o instanceof TupleDesc) {
            TupleDesc compare = (TupleDesc) o;
            Iterator<TDItem> compareIterator = compare.iterator();

            equality = true;
            int i = 0;
            while (compareIterator.hasNext()) {
                if (compare.numFields() != this.items.length) {
                    equality = false;
                    break;
                }
                if (!compareIterator.next().equals(this.items[i])) {
                    equality = false;
                }
                i++;
            }
        }
        return equality;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        String output = "";
        for(int i = 0; i < this.numFields(); i++) {
            output += String.format("%s[%d](%s), ", this.getFieldType(i), i, this.getFieldName(i));
        }
        return output;
    }
}
