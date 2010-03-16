% Calculate the Jensen-Shannon Divergence
function d = jsdivergence(p,q)    
    p=abs(p);
    q=abs(q);
    summand1=kldivergence(p,(p+q)/2);
    summand2=kldivergence(q,(p+q)/2);
    d=0.5*(summand1+summand2);
end

% Kullback Leibler Divergence
function kl = kldivergence(p,q)    
    kl=0;
    for j=1:length(p)
        if q(j)~=0 && p(j)~=0
            kl=kl+(p(j)*log2(abs(p(j)/q(j))));
        end
    end
end