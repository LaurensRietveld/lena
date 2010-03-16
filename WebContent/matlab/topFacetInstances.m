function [resultmix evaldata]=topFacetInstances(start,sptensor,ktensor,i2u,i2p)
resultmix=[];
evaldata=[];
threshold=0.00001;
% for each property
[numPredicates factors]=size(ktensor.U{3});
for i=1:numPredicates
    property=i2p(i);
    propertyIndex=i;    
    if max(ktensor.U{3}(i,:))>0.7 % there is a cluster for this property
        goldindices=gold(start,propertyIndex,sptensor);
        [blvalues, blindices]=baseline(propertyIndex,sptensor);
        [tr1values, tr1indices] = tr1(propertyIndex,ktensor);
        %[tr2values, tr2indices, factors2]=tr2(propertyIndex,ktensor);
        [tr3values, tr3indices]=tr3(propertyIndex,ktensor);
        %[tr4values, tr4indices]=tr4(propertyIndex,ktensor);
        [blsize cols]=size(blindices);
        [tr1size cols]=size(tr1indices);
        %[tr2size cols]=size(tr2indices);
        [tr3size cols]=size(tr3indices');
        %[tr4size cols]=size(tr4indices');    
        % predicate 
        % bl | score | tr1 |  score | tr3 ,{''},{''},{''},{''},{''}
        resultmix=[resultmix;property,{''},{''},{''},{''}];
        for j=1:10
            %fprintf(1,'%2i %d %-50s | %d %-50s | \n', ...
            %    j, blvalues(j),i2u{blindices(j)}, ...
            %    trvalues(j),i2u{trindices(j)});            
            bl='-';
            tr1='-';
            tr1score='-';
            %tr2='-';
            %tr2score='-';
            %tr2f='-';
            tr3='-';
            tr3score='-';
            %tr4='-';
            %tr4score='-';
            if j<=blsize && blvalues(j)>0
                bl=i2u{blindices(j)};
            end
            if j<=tr1size && tr1values(j)>threshold
                tr1=i2u{tr1indices(j)};
                tr1score=tr1values(j);
            end
            %if j<=tr2size
            %    tr2=i2u{tr2indices(j)};
            %    tr2f=factors2(j);
            %    tr2score=tr2values(j);
            %end
            if j<=tr3size && tr3values(j)>threshold
                tr3=i2u{tr3indices(j)};
                tr3score=tr3values(j);
            end        
            %if j<=tr4size
            %    tr4=i2u{tr4indices(j)};
            %    tr4score=tr4values(j);
            %end
            %tr2score,{tr2},tr2f, ,tr4score,{tr4} ,{''},{''},{''},{''},{''}
            resultmix=[resultmix;{bl},tr1score,{tr1},tr3score,{tr3}];            
        end
        resultmix=[resultmix;{''},{''},{''},{''},{''}];
        evaldata=[evaldata;createEvalData(property,goldindices,blvalues,blindices,tr3values,tr3indices,i2u)];
    end    
end
end
    
function evaldata=createEvalData(property,goldindices,blvalues,blindices,trvalues,trindices,i2u)
    threshold=0.00001;    
    top_bl=[];    
    top_tr=[];
    i=1;
    while (i<=10 && blvalues(i)>0)    
        top_bl=[top_bl;blindices(i)];
        i=i+1;
    end
    i=1;
    while (i<=10 && trvalues(i)>threshold)
        top_tr=[top_tr;trindices(i)];
        i=i+1;
    end    
    % union of top bl and top tr indices
    evalindices=union(top_bl,top_tr);
    % count overlaps with gold baseline
    overlappers=intersect(evalindices,goldindices);    
    % remove gold data: evaldata-golddata
    evalindices=setdiff(evalindices,goldindices);
    [numgolds a]=size(goldindices);
    fprintf(1,'property: %s, %d gold, %d BL, %d TR, %d overlaps, \n',...
        property{1},numgolds,nnz(top_bl),nnz(top_tr),nnz(overlappers));
    for i=1:nnz(overlappers)
        fprintf(1,'%s\n',cell2mat(i2u(overlappers(i))));
    end;
    fprintf(1,'\n');
    trset=false;
    % add info who contributed it 1=bl 2=tr 3=both
    for i=1:size(evalindices)
        entry=evalindices(i);
        contributor=0;
        if ismember(entry,top_tr)
            contributor=2;
        end
        if ismember(entry,top_bl)
            contributor=contributor+1;
        end
        if contributor==2
            trset=true;
        end        
        evaldata(i,1)=i2u(entry);
        evaldata{i,2}=contributor;
    end
    if trset==false
        evaldata=[];
    else        
        evaldata={property,evaldata};
    end    
end


function indices=gold(start,propertyIndex,sptensor)
    indices=find(double(sptensor(start,:,propertyIndex)));
end

% baseline method
function [values, indices]=baseline(propertyIndex,sptensor)
    facetMatrix=double(sptensor(:,:,propertyIndex));        
    %[values indices]=sort(facetMatrix,'descend');     
    %fprintf(1,'NNZ:%i\n',nnz(facetMatrix));            
    %count inlinks for each resource
    inlinks=sum(facetMatrix);       
    [values indices]=sortrows(inlinks',-1);     
end


function [values, indices, topfactor]=tr1(propertyIndex,ktensor)
    pfactors=abs(ktensor.U{3}(propertyIndex,:));
    [values,indices]=sort(pfactors,'descend');
    topfactor=indices(1);
    resources=abs(ktensor.U{2}(:,topfactor));
    [values, indices]=sort(resources,'descend');           
end


% TripleRank method
function [resultvalues, resultindices, resultfactors]=tr2(propertyIndex,ktensor) %, resultfactors
    [values,indices]=sortrows(ktensor.U{3}(propertyIndex,:)',-1);        
    otherfactors=(indices(1));
    i=2;    
    % collect factors where property is relevant (>0.6)
    while (values(i)>0.6)
        otherfactors=[otherfactors,indices(i)];
        i=i+1;
    end
    [m factors]=size(otherfactors);
    mixedvalues=[];%values(11:end);
    mixedindices=[];%indices(11:end);
    %rest=size(values)-10;
    mixedfactors=[];%ones(rest(1),1)*topfactor;
    for i=1:factors
        [values, indices]=sortrows(ktensor.U{2}(:,otherfactors(i)),-1);
        [numvalues cols]=size(values);
        mixedvalues=[mixedvalues;values];
        mixedindices=[mixedindices;indices];
        mixedfactors=[mixedfactors;otherfactors(i)*ones(numvalues,1)];
    end    
    mixed=[mixedvalues,mixedindices,mixedfactors]; %
    valuesIndices=sortrows(mixed,-1);
    resultvalues=valuesIndices(:,1);
    resultindices=valuesIndices(:,2);
    resultfactors=valuesIndices(:,3);
end


function [values indices]=tr3(property,ktensor)        
    topp=property;
    % look for top factors for topp
    [values indices]=sort(ktensor.U{3}(topp,:),'descend');
    topfactors=indices(1);
    j=1;
    while j<=size(indices,2) && values(j)>0.6
        if ~ismember(indices(j),topfactors)
            topfactors=[topfactors,indices(j)];
        end
        j=j+1;
    end
    %fprintf(1,'Considering %d factors for property %s\n',j,topp);    
    % resources for topfactors (factor x resource)
    topresources=ktensor.U{2}(:,topfactors)';
    [rows cols]=size(topfactors);
    % add line of -inf for cases where there
    % is only one factor
    [numResources cols]=size(ktensor.U{2});
    topresources=[topresources;-inf(1,numResources)];
    % for each resource,
    % take max for each of relevant factors
    topVR=max(topresources);    
    [values indices]=sort(topVR,'descend');    
end

function [values indices]=tr4(property,ktensor)        
    topp=property;
    % look for top factors for topp
    [values indices]=sort(abs(ktensor.U{3}(topp,:)),'descend');
    topfactors=indices(1);
    j=1;
    % find second and third best factors for topp
    while values(j)>0.4
        if ~ismember(indices(j),topfactors)
            topfactors=[topfactors,indices(j)];
        end
        j=j+1;
    end    
    % resources for topfactors (factor x resource)
    topresources=abs(ktensor.U{2}(:,topfactors))';
    [factors numResources]=size(topresources);
    % multiply factors with score in first topp-factor
    for i=1:factors
        topresources(i,:)=topresources(i,:)*values(i);
    end
    % add line of -inf for cases where there is only one factor    
    topresources=[topresources;-inf(1,numResources)];
    % for each resource,
    % take max for each of relevant factors
    topVR=max(topresources);    
    [values indices]=sort(topVR,'descend');    
end

% function [values indices]=tr4(property,ktensor)        
% %[simscores simprops]
%     [simscores simprops]=similarProperties(property,ktensor);
%     sorted=sortrows([simscores simprops]);
%     topprops=ktensor.U{3}(property,:);
%     i=1;
%     while sorted(i,1)<0.1
%         topprops(i+1,:)=ktensor.U{3}(sorted(i,2),:);
%         i=i+1;
%     end
%     % topprops x resources
%     topresources=topprops*ktensor.U{2}';
%     % is only one factor
%     [numResources cols]=size(ktensor.U{2});
%     topresources=[topresources;-inf(1,numResources)];
%     % for each resource,
%     % take max for each of relevant factors
%     topVR=max(topresources);    
%     [values indices]=sort(topVR,'descend');    
% end


% create cell array containing sorted top n hub-authority-topic entries
function topresults= getTopResults(ktensor,topk,m,props)    
    %factors = size(ktensor.lambda,1); % ranks 
    %topresults=cell(
    topresults(1,:)={'Score','Predicate', ...        
        'Auth-Score','URI', ...        
        'Rank','Weight'};  
    %'Hub-Score','URI', ...  
    [predicates factors]=size(ktensor.U{3});
    % for each factor
    for i=1:factors
            %[v1 i1]=sort(ktensor.U{1}(:,i),-1); % hubs
            [v2 i2]=sort(ktensor.U{2}(:,i),'descend'); % authoritive resources
            [v3 i3]=sort(ktensor.U{3}(:,i),'descend'); % predicates
            for k=1:topk                
                if k>predicates
                    predData=[0,'-'];                    
                else
                    predData=[v3(k),props(i3(k))];
                end
                topresults=[topresults; predData, ...                   
                    v2(k),m(i2(k)), ...                    
                    i,ktensor.lambda(i)];
                % v1(k),m(i1(k)), ...
            end                            
    end
end

function [simScores similarProps]=similarProperties(p,ktensor)
    [props factors]=size(ktensor.U{3});
    pVector=ktensor.U{3}(p,:);
    simScores=inf((props)-1,1);
    similarProps=zeros(props-1,1);
    j=1;
    for i=1:props
        if i~=p
            simScores(j)=jsdivergence(pVector,ktensor.U{3}(i,:));
            similarProps(j)=i;
            j=j+1;
        end
    end
end
            

% TripleRank method
% function [resultvalues, resultindices, resultfactors]=tr4(propertyIndex,ktensor)
%     resultvalues=[];
%     resultindices=[];
%     resultfactors=[];
%     [factorvalues,factorindices]=sortrows(ktensor.U{3}(propertyIndex,:)',-1);        
%     topfactors=[];    
%     % collect factors where property is relevant (>0.4)
%     i=1;
%     while (values(i)>0.4)
%         topfactors=[topfactors,factorindices(i)];
%         i=i+1;
%     end
%     topResources=ktensor.U{3}(topfactors,:)*ktensor.U{2}(topfactors,:)';
%     for i=1:20
%         
%     
%     resultvalues=valuesIndices(:,1);
%     resultindices=valuesIndices(:,2);
%     resultfactors=valuesIndices(:,3);
% end