function [spt pstat rstat]=transform(spt,pstat,rstat,startr)       
    [spt pstat rstat]=prunePredicates(spt,pstat,rstat);       
    [spt rstat]=removeInlinks(startr,spt,rstat);  
    %rstat=updaterstat(spt,rstat);    
    [spt rstat]=pruneResources(spt,rstat); 
    %pstat=updatepstat(spt,pstat);
    %rstat=updaterstat(spt,rstat);
    %tic;
    spt=wheighResources(spt,rstat);
    %toc;
    [spt pstat]=wheighPredicates(spt,pstat);
end

function [spt pstat]= wheighPredicates(spt,pstat)
    maxlinks=max(cell2mat(pstat(:,2)));
    [numlinks,cols]=size(pstat);
    for i=1:numlinks
        % calculate weight multiplicator      
        multiplicator=1+log(maxlinks/pstat{i,2}); 
        spt(:,:,i)=spt(:,:,i)*multiplicator;
    end
end

function pstat = updatepstat(spt,pstat)
    tsize=size(spt);        
    np=tsize(3);
    diff=0;
    tic;
    for i=1:np
        old=pstat{i,2};
        new=nnz(spt(:,:,i));
        diff=old-new;
        pstat{i,2}=new;
    end
    toc;
    fprintf(1,'Diff is %d\n',diff);
    
end

function rstat = updaterstat(spt,rstat)
    tsize=size(spt);    
    nr=tsize(1);
    diff=0;
    tic;
    for i=1:nr
        if mod(i,1000)==0
            fprintf(1,'.');
        end
        old=rstat{i,3};
        newIn=nnz(spt(:,i,:));
        newOut=nnz(spt(i,:,:));
        %diff=old-(newIn+newOut);
        rstat{i,3}=newIn+newOut;
    end
    fprintf(1,'\n');
    toc;    
    %fprintf(1,'Diff is %d\n',diff);
end

function [spt pstat rstat ]=prunePredicates(spt,pstat,rstat)   
    numlinkVector=cell2mat(pstat(:,2));
    totalLinks=sum(numlinkVector);    
    upperbound=0.4*totalLinks;
    lowerbound=0.0001*totalLinks;
    [m1 n2]=find(numlinkVector>upperbound);
    [m2 n2]=find(numlinkVector<lowerbound);
    deleteInd=[m1;m2];    
    if ~isempty(deleteInd)  
        [spt pstat rstat]=removePredicates(deleteInd,spt,pstat,rstat);
    end
end

function [newTensor pstat rstat] = removePredicates(pIndices,spt,pstat,rstat) 
    todelete=sort(pIndices);
    [numdeletes n]=size(todelete);
    % create new tensor of size mxnx(o-pIndices)
    oldSize=size(spt);
    newSize=[oldSize(1),oldSize(2),oldSize(3)-numdeletes];
    newTensor=sptensor(newSize);
    deleteIndex=1;
    pIndex=1;
    % copy old tensor to new tensor skipping delete props
    for i=1:oldSize(3)
        if (i==todelete(deleteIndex)) 
            % skip             
            if deleteIndex==numdeletes
                deleteIndex=1;
            else
                deleteIndex=deleteIndex+1;
            end            
        else
            % copy
            newTensor(:,:,pIndex)=spt(:,:,i);
            pIndex=pIndex+1;
        end        
    end
    % update pstat
    i=numdeletes;
    diff=0;
    while i>0    
        p=pstat{todelete(i),1};
        l=pstat{todelete(i),2};
        lcheck=nnz(spt(:,:,todelete(i)));
        diff=diff+(l-lcheck);
        
        [I J]=find(spt(:,:,todelete(i)));
        for j=1:lcheck
            % update rstat
            % reduce outlink of I(j) by 1
            rstat{I(j),2}=rstat{I(j),2}-1;
            rstat{I(j),3}=rstat{I(j),3}-1;
            % reduce inlink of J(j) by 1
            rstat{J(j),2}=rstat{J(j),1}-1;
            rstat{J(j),3}=rstat{J(j),3}-1;            
        end        
        fprintf(1,'Removing predicate %d %s (%d) (%d)\n', ...
                todelete(i),p,l,lcheck); 
        pstat(todelete(i),:)=[];
        i=i-1;
    end
    fprintf(1,'Diff is %d\n',diff);
end

function [newspt newrstat]=pruneResources(spt,rstat)
    oldSize=size(spt);           
    degrees=cell2mat(rstat(:,3));       
    % for resources with minimal degree (in+out)
    indices=getQualifiedResources(degrees,oldSize(3));
    [newNumResources cols]=size(indices);
    % create new tensor of size newNumResources x newNumResources x o
    newspt=sptensor([newNumResources,newNumResources,oldSize(3)]);
    % remove collected resources by tensor copy (see predicate pruning)
    % - skip copying of spt(resource,resource,:);
    newrstat=cell(newNumResources,4);
    newRIndex=1;
    fprintf(1,'Creating new tensor for %d resources ...\n',newNumResources);
    tstart = tic;
    for i=1:newNumResources
        %[totaldegree indegree outdegree]=getDegree(indices(i),spt);
        %fprintf(1,' In: %d Out: %d Total:%d',indegree,outdegree,totaldegree);
        % copy
        newspt(newRIndex,:,:)=spt(indices(i),indices,:);
        newspt(:,newRIndex,:)=spt(indices,indices(i),:);                
        newrstat{newRIndex,4}=rstat{indices(i),4};       
        newRIndex=newRIndex+1;
    end
    telapsed = toc(tstart);
    fprintf(1,' done in %d seconds\n',telapsed);
end

function spt = wheighResources(spt,rstat)
    degrees=rstat(:,3);
    maxDegree=max(cell2mat(degrees));
    % make sum of indegrees=1 for each resources
    tsize=size(spt);
    for i=1:tsize(1)
        %numIn=nnz(spt(:,i,:));
        %numOut=nnz(spt(i,:,:));
        d=degrees{i};
        if d ~= 0
            factor=1+log(maxDegree/d);
            spt(:,i,:)=spt(:,i,:)*factor;
        end
        %fprintf(1,'.');
    end
    fprintf(1,'\n');
end

function indices = getQualifiedResources(degrees,numPreds)               
    indices=find(degrees>0);
    resources=size(indices,1);
    x=resources*resources*numPreds;
    d=1;
    while (x>1.0000e+10)
        indices=find(degrees>d);
        resources=size(indices,1);
        x=resources*resources*numPreds;
        d=d+1;
    end
end

function [spt rstat]=removeInlinks(resource,spt,rstat)
    s=size(spt);    
    numInlinks=nnz(spt(:,resource,:));
    spt(:,resource,:)=0;
    rInlinks=rstat{resource,1}-numInlinks;
    rstat{resource,1}=rInlinks;
    rstat{resource,3}=rstat{resource,3}-numInlinks;
end

