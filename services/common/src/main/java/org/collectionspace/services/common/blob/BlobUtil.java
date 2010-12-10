package org.collectionspace.services.common.blob;

import org.collectionspace.services.common.context.ServiceContext;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobUtil {
	//FIXME: REM - We should have a class/interface in common that has constant defs for the names of all the services.
	public static String BLOB_RESOURCE_NAME = "blobs";
	private static final Logger logger = LoggerFactory.getLogger(BlobUtil.class);
	
    public static BlobInput getBlobInput(ServiceContext<MultipartInput, MultipartOutput> ctx) {
    	BlobInput result = (BlobInput)ctx.getProperty(BlobInput.class.getName());
    	if (result == null) {
    		result = new BlobInput();
    		setBlobInput(ctx, result);
    	}
    	return result;
	}
    
    public static BlobInput resetBlobInput(ServiceContext<MultipartInput, MultipartOutput> ctx) {
    	BlobInput blobInput = new BlobInput();
    	setBlobInput(ctx, blobInput);
    	return blobInput;
    }
    
    public static void setBlobInput(ServiceContext<MultipartInput, MultipartOutput> ctx,
    		BlobInput blobInput) {
    	ctx.setProperty(BlobInput.class.getName(), blobInput);    		
	}	
    
}