WITH counts AS (SELECT E.sid, COUNT(*) num  FROM enroll E GROUP BY E.sid)

SELECT S.sid, S.sname 
    FROM student S, counts
    WHERE S.sid = counts.sid
    AND counts.num = (SELECT MAX(num) FROM counts);
