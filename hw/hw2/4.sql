SELECT C.cno, C.cname, E.sectno FROM course C
    JOIN enroll E ON E.cno = C.cno AND E.dname = C.dname -- LEFT for null sect
    GROUP BY C.cno, C.cname, E.sectno
    HAVING COUNT(E.cno) < 6;
