package net.floodlightcontroller.bgproute;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.restserver.IRestApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BgpRoute implements IFloodlightModule, IBgpRouteService {
	
	protected static Logger log = LoggerFactory.getLogger(BgpRoute.class);

	protected IFloodlightProviderService floodlightProvider;
	
	protected static Ptree ptree;
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IBgpRouteService.class);
		return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		m.put(IBgpRouteService.class, this);
		return m;
	}

	protected IRestApiService restApi;
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IBgpRouteService.class);
		return l;
	}
	
	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
	    
	    ptree = new Ptree(32);
		
		// Register floodlight provider and REST handler.
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		
		// Test.
		//test();
	}

	public Ptree getPtree() {
		return ptree;
	}
	
	// Return nexthop address as byte array.
	public byte[] lookupRib(byte[] dest) {
		if (ptree == null) {
		    log.debug("lookupRib: ptree null");
		    return null;
		}
		
		PtreeNode node = ptree.match(dest, 32);
		if (node == null) {
            log.debug("lookupRib: ptree node null");
			return null;
		}
		if (node.rib == null) {
            log.debug("lookupRib: ptree rib null");
			return null;
		}
		ptree.delReference(node);
		
		return node.rib.nextHop.getAddress();
	}
	
	@SuppressWarnings("unused")
    private void test() {
		System.out.println("Here it is");
		Prefix p = new Prefix("128.0.0.0", 8);
		Prefix q = new Prefix("8.0.0.0", 8);
		Prefix r = new Prefix("10.0.0.0", 24);
		Prefix a = new Prefix("10.0.0.1", 32);
	
		ptree.acquire(p.getAddress(), p.masklen);
		ptree.acquire(q.getAddress(), q.masklen);
		ptree.acquire(r.getAddress(), r.masklen);
	
		System.out.println("Traverse start");
		for (PtreeNode node = ptree.begin(); node != null; node = ptree.next(node)) {
			Prefix p_result = new Prefix(node.key, node.keyBits);
		}
	
		PtreeNode n = ptree.match(a.getAddress(), a.masklen);
		if (n != null) {
			System.out.println("Matched prefix for 10.0.0.1:");
			Prefix x = new Prefix(n.key, n.keyBits);
			ptree.delReference(n);
		}
		
		n = ptree.lookup(p.getAddress(), p.masklen);
		if (n != null) {
			ptree.delReference(n);
			ptree.delReference(n);
		}
		System.out.println("Traverse start");
		for (PtreeNode node = ptree.begin(); node != null; node = ptree.next(node)) {
			Prefix p_result = new Prefix(node.key, node.keyBits);
		}
		
		n = ptree.lookup(q.getAddress(), q.masklen);
		if (n != null) {
			ptree.delReference(n);
			ptree.delReference(n);
		}
		System.out.println("Traverse start");
		for (PtreeNode node = ptree.begin(); node != null; node = ptree.next(node)) {
			Prefix p_result = new Prefix(node.key, node.keyBits);
		}
		
		n = ptree.lookup(r.getAddress(), r.masklen);
		if (n != null) {
			ptree.delReference(n);
			ptree.delReference(n);
		}
		System.out.println("Traverse start");
		for (PtreeNode node = ptree.begin(); node != null; node = ptree.next(node)) {
			Prefix p_result = new Prefix(node.key, node.keyBits);
		}

	}
	
	@Override
	public void startUp(FloodlightModuleContext context) {
		restApi.addRestletRoutable(new BgpRouteWebRoutable());
	}
}