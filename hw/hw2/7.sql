
SELECT S.sid, S.sname FROM student S, enroll E
    WHERE S.sid = E.sid
      AND E.dname = 'Computer Science'

INTERSECT

SELECT S.sid, S.sname FROM student S, enroll E
    WHERE S.sid = E.sid
      AND E.dname = 'Mathematics';
