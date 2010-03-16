% create cell array containing sorted top n hub-authority-topic entries
function topresults= getResults(ktensor,topk,m,props)    
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
                value='-';
                    prop='-';
                if k<=predicates && v3(k)>0.1
                        value=v3(k);
                        prop=props(i3(k));                                       
                end
                if v2(k)>0.001                    
                    topresults=[topresults; value,prop, ...                   
                        v2(k),m(i2(k)), ...                    
                        i,ktensor.lambda(i)];
                        % v1(k),m(i1(k)), ...
                else
                    topresults=[topresults; value,prop, ...                   
                        {'-'},{'-'}, ...                    
                        i,ktensor.lambda(i)];
                end
            end                            
    end
end

% create cell array containing sorted top n hub-authority-topic entries
function topresults=getResults(ktensor,topk,m,props)        
    %topresults=getTopResults(ktensor,topk,m,props);
    topresults(1,:)={'Predicate','Score','URI'};  
    [predicates factors]=size(ktensor.U{3});
    donePredicates=[];
    % for each factor    
    for i=1:factors
            % get top predicate
            [v3 i3]=sort(ktensor.U{3}(:,i),'descend'); % predicates
            j=1;
            while v3(j)>0.4
                topp=i3(j);
                if ~ismember(topp,donePredicates)
                    donePredicates=[donePredicates topp];
                    %fprintf(1,'\n** %s **\n',props{topp});                
                    % multiply p-scores with r-scores 
                    toppV=ktensor.U{3}(topp,:);
                    topVR=toppV*ktensor.U{2}';
                    % choose the top 10
                    [values indices]=sort(topVR,'descend');
                    for k=1:topk
                        score=values(k);
                        if score>0.0001 
                            row=cell(1,3);
                            row{1,3}=m{indices(k)};
                            row{1,2}=score;                            
                            if (k==1)
                                row{1,1}=props{topp};
                            end
                            topresults=[topresults;row];
                            %fprintf(1,'%s\n',m{indices(k)});
                        end            
                    end
                %else
                %    fprintf(1,'Skipping %s\n',props{topp});
                end
                j=j+1;
            end
    end
end
