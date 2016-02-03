
SELECT M.dname,  AVG(S.gpa) FROM student S, major M
    WHERE S.sid = M.sid
      AND M.dname IN (
         SELECT DISTINCT M.dname FROM student S, major M
            WHERE S.gpa < 1 AND S.sid = M.sid
        )
    GROUP BY M.dname;

