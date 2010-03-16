function numLinks=numLinks(predicate,spt,i2p)
    pi=index4uri(predicate,i2p);
    numLinks=nnz(spt(:,:,pi));
end