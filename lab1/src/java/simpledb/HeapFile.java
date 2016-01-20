package simpledb;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File file;
    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        this.file = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        return this.file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        try {
            int pageSize = BufferPool.getPageSize();
            RandomAccessFile f = new RandomAccessFile(this.file, "r");
            byte[] data = new byte[pageSize];

            int position = pid.pageNumber() * pageSize;
            f.read(data, position, pageSize);

            HeapPageId hpageId = new HeapPageId(pid.getTableId(), pid.pageNumber());
            return new HeapPage(hpageId, data);
        } catch (Exception e) {
            // TODO: think about this
            e.printStackTrace();
            return null;
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int) Math.ceil(this.file.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // TODO: helper class
    private class HeapFileIterator implements DbFileIterator {
        private TransactionId tid;
        private HeapFile heapFile;
        // private Tuple[] tuples;
        private ArrayList<Tuple> tuples;

        private int currentPage;
        private int currentTuple;

        HeapFileIterator(TransactionId tid, HeapFile heapFile) {
            this.tid = tid;
            this.heapFile = heapFile;
        }

        public void open() throws DbException, TransactionAbortedException {
            this.tuples = new ArrayList<Tuple>();
            this.currentPage = 0;
            this.addTuplesFromPage(this.currentPage);
        }

        private void addTuplesFromPage (int pageNumber) throws DbException, TransactionAbortedException {
            this.currentTuple = -1;
            this.tuples.clear();
            HeapPageId nextPageId = new HeapPageId(this.heapFile.getId(), pageNumber);
            HeapPage nextPage = (HeapPage) Database.getBufferPool().getPage(this.tid, nextPageId, Permissions.READ_ONLY);
            nextPage.iterator().forEachRemaining(this.tuples::add); // FIXME
        }

        public boolean hasNext() throws DbException, TransactionAbortedException {
            return this.tuples != null  && (this.currentPage < this.heapFile.numPages() - 1 || this.currentTuple < this.tuples.size() - 1);
        }

        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (this.hasNext() && this.currentTuple < this.tuples.size() - 1) {
                this.currentTuple++;
                return this.tuples.get(this.currentTuple);
            } else if (this.hasNext()) {
                this.currentPage++;
                this.addTuplesFromPage(this.currentPage);
                return this.tuples.get(this.currentTuple);
            } else {
                throw new NoSuchElementException();
            }
        }

        public void rewind() throws DbException, TransactionAbortedException {
            this.close();
            this.open();
        }

        public void close() {
            this.tuples = null;
        }
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return new HeapFileIterator(tid, this);
    }

}
