function matrix = getMatrix(fileName,mname) 
    % read matrix file     
    s=['load ' fileName];
    eval(s);
    % spconvert for matrix name
    s=['spconvert(' mname ')']; 
    matrix = eval(s);
end