SELECT MAX(S.age) - MIN(S.age) as range  FROM student S, major M
    WHERE S.sid = M.sid AND M.dname = 'Computer Science';
