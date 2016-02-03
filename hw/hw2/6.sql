
SELECT DISTINCT M.dname FROM student S, major M
    WHERE M.sid = S.sid AND S.age < 18;
