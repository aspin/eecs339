SELECT E.cno, E.sectno, AVG(S.gpa) FROM enroll E, student S
    WHERE E.dname = 'Computer Science' AND S.sid = E.sid
    GROUP BY E.cno, E.sectno;
