-- As per Piazza, LEFT JOIN to also include the class with no students in enrolled
-- Remove the LEFT for the answer for only classes with students
SELECT C.cno, C.cname, E.sectno FROM course C
    LEFT JOIN enroll E ON E.cno = C.cno AND E.dname = C.dname
    GROUP BY C.cno, C.cname, E.sectno
    HAVING COUNT(E.cno) < 6;
