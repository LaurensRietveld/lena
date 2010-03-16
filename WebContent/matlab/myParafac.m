function [ktensor newspt newpstat newrstat resultmix evaldata spt] ...
        = myParafac(starturi,prefix,display,store,factors,dir) 
    [spt pstat rstat]=getTensor(prefix,dir);
    %newspt=spt;
    %newpstat=pstat;
    %newrstat=rstat;    
    startrindex=index4uri(starturi,rstat(:,4));
    [newspt newpstat newrstat]=transform(spt,pstat,rstat,startrindex);
    startrindex=index4uri(starturi,newrstat(:,4));
    %marray=tensor2array(newspt);
    
    % default number of factors for parafac decomposition
    
    f=estimateFactor(newspt,newpstat);
    if exist('factors','var')
       f=factors;
    end
    tic;
    ktensor=parafac_als(newspt,f);      
    toc;
    predicates=newpstat(:,1);
    labels=newrstat(:,4);
    
    [resultmix evaldata]=topFacetInstances(startrindex,newspt,ktensor,labels,predicates);
    if exist('store','var')
        if store==true               
            cell2csv([constants.resultsDir prefix '_tripleRank_' int2str(f) '.csv'],resultmix,',',98);
            evalmap=[];
            results=getResults(ktensor,10,labels,predicates);
            cell2csv([constants.resultsDir prefix '_tripleRank_' int2str(f) '_results.csv'],results,',',98);
            for i=1:size(evaldata)
                evalmap=[evalmap;i,evaldata{i,1}];
                cell2csv([constants.resultsDir prefix '_tripleRank_' int2str(f) '_eval' int2str(i) '.csv'],evaldata{i,2},',',98);
            end
            cell2csv([constants.resultsDir prefix '_tripleRank_' int2str(f) '_evalmap.csv'],evalmap,',',98);
        end
    end    
    
    if exist('display','var')
        if display==true    
            datadisp(ktensor,{labels,labels,predicates})    
        end
    end    
end

function factor = estimateFactor(spt,pstat)
    % factor is the number of predicates that
    % have more than 5% of the statements
    % times the number of predicates with
    % more than 10% of all statements
    sperpred=cell2mat(pstat(:,2));
    maxS=max(sperpred);
    bound5=0.05*maxS;
    bound10=0.10*maxS;
    numfivers=0;
    numteners=0;
    for i=1:size(sperpred,1)
        pred=sperpred(i);
        if pred>bound10
            numteners=numteners+1;
        else if pred>bound5
                numfivers=numfivers+1;
            end
        end
    end
    factor=(numfivers+numteners)*numteners;
end

function marray = tensor2array(spt)
    tsize=size(spt);
    marray=sparse(tsize(1),tsize(1)*tsize(3));
    for i=1:tsize(3)
        % take slice, transform to sparse matrix, add to marray
        from=(i-1)*tsize(2)+1;
        to=i*tsize(2);
        marray(:,from:to)=sparse(double(spt(:,:,i)));
    end
end