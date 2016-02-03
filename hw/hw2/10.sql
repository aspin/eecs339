
SELECT S.sid, S.sname, S.gpa FROM student S
    WHERE S.sid IN (
SELECT E.sid FROM enroll E
    WHERE E.dname = 'Civil Engineering'
    GROUP BY E.sid
    HAVING COUNT(*) = (SELECT COUNT(*) FROM course WHERE dname = 'Civil Engineering')
);
