package cz.incad.cdk.cdkharvester.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Iterator for one title
 * @author pstastny
 */
public class TitleCDKHarvestIterationImpl extends AbstractCDKHarvestIteration {

    public static final Logger LOGGER = Logger.getLogger(TitleCDKHarvestIterationImpl.class.getName());
    private String baseUrl;
    private String topPid;
    private List<CDKHarvestIterationItem> processingList;

    public TitleCDKHarvestIterationImpl(String k4Url, String pid)
            throws CDKHarvestIterationException {
        this.baseUrl = k4Url;
        this.topPid = pid;
        this.processingList = new LinkedList<>();
        this.processingList.add(new CDKHarvestIterationItemImpl(pid, null));
    }


    private String itemURL(String k4Url, String pid) {
        return k4Url + "/api/v5.0/item/"+pid;
    }

    private String childrenURL(String k4Url, String pid) {
        return k4Url + "/api/v5.0/item/"+pid+"/children";
    }


    @Override
    public void init() throws CDKHarvestIterationException {
        try {
            // deti
            Stack<String> stack = new Stack<>();
            stack.push(this.topPid);
            while(!stack.isEmpty()) {
                String p = stack.pop();
                JSONArray childrenResults = childrenResults(childrenURL(this.baseUrl,p));
                for (int i = 0,ll=childrenResults.length(); i < ll; i++) {
                    JSONObject jsonObject = childrenResults.getJSONObject(i);
                    String childP = jsonObject.getString("pid");
                    stack.push(childP);
                    LOGGER.info("adding child pid "+childP);
                    this.processingList.add(new CDKHarvestIterationItemImpl(childP, null));
                }
            }

            // cesta nahoru
            JSONObject itemResult = itemResult(itemURL(this.baseUrl,this.topPid));
            JSONArray jsonArray = itemResult.getJSONArray("context");
            for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
                JSONArray path = jsonArray.getJSONArray(i);
                for (int j = 0,lj=path.length(); j < lj; j++) {
                    JSONObject jsonObject = path.getJSONObject(j);
                    String pid = jsonObject.getString("pid");
                    if(!this.topPid.equals(pid)) {
                        LOGGER.info("adding ctx pid "+pid);
                        this.processingList.add(new CDKHarvestIterationItemImpl(pid, null));
                    }
                }
            }

        } catch (IOException e) {
            throw new CDKHarvestIterationException(e);
        }
    }

    @Override
    public boolean hasNext() throws CDKHarvestIterationException {
        return !this.processingList.isEmpty();
    }

    @Override
    public CDKHarvestIterationItem next() throws CDKHarvestIterationException {
        while(!this.processingList.isEmpty()) {
            CDKHarvestIterationItem removed = this.processingList.remove(0);
            return removed;
        }
        return null;
    }

    public JSONObject itemResult(String urlStr) throws IOException {
        try {
            WebResource r = client(urlStr);
            try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
                return json(is, JSONObject.class);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Retrying...", ex);
                try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
                    return json(is, JSONObject.class);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new IOException(e);
        } catch (SecurityException e) {
            throw new IOException(e);
        } catch (InstantiationException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        } catch (UniformInterfaceException e) {
            throw new IOException(e);
        } catch (ClientHandlerException e) {
            throw new IOException(e);
        }
    }


    public JSONArray childrenResults(String urlStr) throws IOException {
        try {
            WebResource r = client(urlStr);
            try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
                return json(is, JSONArray.class);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Retrying...", ex);
                try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
                    return json(is, JSONArray.class);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new IOException(e);
        } catch (SecurityException e) {
            throw new IOException(e);
        } catch (InstantiationException e) {
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            throw new IOException(e);
        } catch (UniformInterfaceException e) {
            throw new IOException(e);
        } catch (ClientHandlerException e) {
            throw new IOException(e);
        }
    }

    private <T> T json(InputStream is, Class<T> clz) throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String str = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        Constructor<T> constructor = clz.getConstructor(String.class);
        T ret = constructor.newInstance(str);
        return ret;
    }

}