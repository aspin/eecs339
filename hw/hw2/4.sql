SELECT C.cno, C.cname, E.sectno FROM course C
    LEFT JOIN enroll E ON E.cno = C.cno AND E.dname = C.dname -- LEFT for null sect, as per piazza
    GROUP BY C.cno, C.cname, E.sectno
    HAVING COUNT(E.cno) < 6;
