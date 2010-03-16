function [total indegree outdegree]=getDegree(r,spt)
    indegree=sum(nnz(spt(:,r,:)));
    outdegree=sum(nnz(spt(r,:,:)));
    total=indegree+outdegree;
end