package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId transactionId;
    private DbIterator child;

    private TupleDesc resultDescriptor;
    private boolean fetched;
    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        this.transactionId = t;
        this.child = child;

        Type[] types = new Type[] { Type.INT_TYPE };
        String[] fields = new String[] { null };
        this.resultDescriptor = new TupleDesc(types, fields);
        this.fetched = false;
    }

    public TupleDesc getTupleDesc() {
        return this.resultDescriptor;
    }

    public void open() throws DbException, TransactionAbortedException {
        this.child.open();
        super.open();
    }

    public void close() {
        super.close();
        this.child.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        this.child.rewind();
        this.fetched = false;
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (!this.fetched) {
            int count = 0;
            while (this.child.hasNext()) {
                Tuple tuple = this.child.next();
                try {
                    Database.getBufferPool().deleteTuple(this.transactionId, tuple);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new TransactionAbortedException();
                }
                count++;
            }
            Tuple resultTuple = new Tuple(this.resultDescriptor);
            resultTuple.setField(0, new IntField(count));
            this.fetched = true;
            return resultTuple;
        } else {
            return null;
        }
    }

    @Override
    public DbIterator[] getChildren() {
        return new DbIterator[] { this.child };
    }

    @Override
    public void setChildren(DbIterator[] children) {
        this.child = children[0];
    }

}
