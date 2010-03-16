function [spt pstat rstat]=getTensor(prefix,dir)   
    dataDir=[dir constants.dataDir prefix '/'];
    fprintf(1,dataDir);
    pstat=getPstat(prefix,dataDir);
    [numPredicates cols]=size(pstat); 
    rstat=getResourceStat(prefix,dataDir);    
    % create the subs array holding coordinates of nnz entries 
    % of the tensor
    subs=[];
    % values for the stored coordinates
    vals=[];  
    % consider all links 
    for i=1:numPredicates
        matrix=getLinkMatrix(dataDir,pstat{i,3});        
        % store coordinates and values 
        [ms ns]=find(matrix);
        nonzeroIndices=[ms ns];
        nzs=size(ms);
        x=ones(nzs(1),1);            
        subs=[subs; nonzeroIndices x*i]; 
        values=ones(nzs(1),1);      
        % add values to vals
        vals=[vals;values];
    end
    numResources=size(matrix);
    % create a sparse tensor 
    spt = sptensor(subs,vals,[numResources i]);
end

function pstat=getPstat(prefix,dataDir)    
    fileList=[dataDir  prefix '_matrices.csv'];
    fid=fopen(fileList);
    % matrices = filename | p-uri | numlinks
    matrices=textscan(fid,'%s%s%f','Delimiter',',');
    fclose(fid);
    [rows cols]=size(matrices{2});
    pstat=cell(rows,3);
    for i=1:rows
        pstat{i,1}=matrices{2}{i};
        pstat{i,2}=matrices{3}(i); 
        pstat{i,3}=matrices{1}{i};
    end
end

function rdata = getResourceStat(prefix,dataDir)
		%dataDir='/home/franz/dev/eclipseWS/surfrank/data/matrices/';
    %dataDir='/home/jmkoch/workspace/lenaTripleRank/data/matrices/';
    mapfile=[dataDir prefix '_mapping.csv'];
    fprintf(1,mapfile);
    fid=fopen(mapfile);    
    % index | uri | indegree | outdegree
    statistics=textscan(fid,'%d%s%d%d','Delimiter',';');
    fclose(fid);
    [rows cols]=size(statistics{2});
    rdata=cell(rows,4);
    for i=1:rows
        rdata{i,1}=statistics{3}(i);
        rdata{i,2}=statistics{4}(i);
        rdata{i,3}=statistics{4}(i)+statistics{3}(i);
        rdata{i,4}=statistics{2}{i};  
    end
end

function matrix = getLinkMatrix(dataDir, file)
    %fileName=strcat(dataDir,matrices{1}(index));
    fileName=strcat(dataDir,file);    
    matrixname=strtok(file,'.');
    matrix=getMatrix(fileName,matrixname);    
end