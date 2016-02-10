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
            f.seek(position);
            f.read(data, 0, pageSize);
            f.close();

            HeapPageId hpageId = new HeapPageId(pid.getTableId(), pid.pageNumber());
            return new HeapPage(hpageId, data);
        } catch (Exception e) {
            // TODO: think about this
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        int pageSize = BufferPool.getPageSize();
        RandomAccessFile f = new RandomAccessFile(this.file, "rw");
        byte[] data = new byte[pageSize];
        data = page.getPageData();

        int position = page.getId().pageNumber() * pageSize;
        f.seek(position);
        f.write(data, 0, pageSize);
        f.close();
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int) Math.ceil(1.0 * this.file.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        ArrayList<Page> modifiedPages = new ArrayList<Page>();
        Page openPage = this.getOpenPage(tid);

        if (openPage == null) {
            HeapPageId newPageId = new HeapPageId(this.getId(), this.numPages());
            HeapPage newPage = new HeapPage(newPageId, HeapPage.createEmptyPageData());
            newPage.insertTuple(t);
            this.writePage(newPage);
            modifiedPages.add(newPage);
        } else {
            HeapPage openHeapPage = (HeapPage) openPage;
            openHeapPage.insertTuple(t);
            modifiedPages.add(openHeapPage);
        }

        return modifiedPages;
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        ArrayList<Page> modifiedPages = new ArrayList<Page>();
        PageId expectedPageId = t.getRecordId().getPageId();
        HeapPage expectedHeapPage = (HeapPage) Database.getBufferPool().getPage(tid, expectedPageId, Permissions.READ_WRITE);

        expectedHeapPage.deleteTuple(t);
        modifiedPages.add(expectedHeapPage);
        return modifiedPages;
    }

    public Page getOpenPage(TransactionId transactionId) throws DbException, TransactionAbortedException{
        for(int i = 0; i < this.numPages(); i++) {
            HeapPageId heapPageId = new HeapPageId(this.getId(), i);
            HeapPage heapPage = (HeapPage) Database.getBufferPool().getPage(transactionId, heapPageId, Permissions.READ_ONLY);
            if (heapPage.getNumEmptySlots() > 0) {
                return heapPage;
            }
        }
        return null;
    }

    // Helper Iterator Class
    // TODO: consider pulling out into separate file
    private class HeapFileIterator implements DbFileIterator {
        private TransactionId tid;
        private HeapFile heapFile;
        private Iterator<Tuple> tuples;

        private int currentPage;

        HeapFileIterator(TransactionId tid, HeapFile heapFile) {
            this.tid = tid;
            this.heapFile = heapFile;
        }

        public void open() throws DbException, TransactionAbortedException {
            this.currentPage = 0;
            this.tuples = this.addTuplesFromPage(this.currentPage).iterator();
        }

        private ArrayList<Tuple> addTuplesFromPage (int pageNumber) throws DbException, TransactionAbortedException {
            ArrayList<Tuple> tuples = new ArrayList<Tuple>();
            HeapPageId nextPageId = new HeapPageId(this.heapFile.getId(), pageNumber);
            HeapPage nextPage = (HeapPage) Database.getBufferPool().getPage(this.tid, nextPageId, Permissions.READ_ONLY);
            nextPage.iterator().forEachRemaining(tuples::add);
            return tuples;
        }

        public boolean hasNext() throws DbException, TransactionAbortedException {
            // REFACTOR ME
            if (this.tuples != null && this.tuples.hasNext()) {
                return true;
            } else if (this.tuples != null && this.currentPage < this.heapFile.numPages() - 1) {
                ArrayList<Tuple> tuples = this.addTuplesFromPage(this.currentPage + 1);
                return tuples.size() > 0;
            } else {
                return false;
            }
        }

        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
            if (this.tuples == null) {
                throw new NoSuchElementException();
            }
            if (this.tuples.hasNext()) {
                return this.tuples.next();
            } else if (!this.tuples.hasNext() && this.currentPage < this.heapFile.numPages() - 1) {
                this.currentPage++;
                this.tuples = this.addTuplesFromPage(this.currentPage).iterator();
                if (this.tuples.hasNext()) {
                    return this.next();
                } else {
                    throw new NoSuchElementException();
                }

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
