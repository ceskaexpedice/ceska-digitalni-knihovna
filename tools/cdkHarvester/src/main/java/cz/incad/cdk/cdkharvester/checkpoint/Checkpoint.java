package cz.incad.cdk.cdkharvester.checkpoint;

import cz.incad.kramerius.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class Checkpoint {

    public static final SimpleDateFormat STANDARD_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat WO_MILLISECONDS_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected Date currentCheckpointDate;

    public Date get() {
        return this.currentCheckpointDate;
    }


    public void update(String date) throws CheckpointException {
//        try {
//            Date parsedDate = this.parseDate(date);
//            this.currentCheckpointDate = this.currentCheckpointDate.before(parsedDate) ? parsedDate : this.currentCheckpointDate;
//        } catch ( ParseException ex) {
//            throw new CheckpointException(ex);
//        }
    }


//    private boolean dateLimitTouched(String testingDate) throws ParseException {
//        if (this.dateLimit != null && StringUtils.isAnyString(this.dateLimit)) {
//            Date parsedActual = null;
//            try {
//                parsedActual = STANDARD_SIMPLE_DATE_FORMAT.parse(testingDate);
//            } catch (ParseException e) {
//                parsedActual = WO_MILLISECONDS_SIMPLE_DATE_FORMAT.parse(testingDate);
//            }
//            Date parsedLimit = STANDARD_SIMPLE_DATE_FORMAT.parse(this.dateLimit);
//            boolean after = parsedActual.after(parsedLimit);
//            if (after) {
//                logger.info("Date limit touched "+testingDate);
//                return true;
//            }
//        }
//        return false;
//    }

//    public  String formatDate(Date date) {
////        Date parsedActual = null;
////        return new SimpleDateFormat(DATE_FORMAT_SMALL).format(date).getBytes(Charset.forName("UTF-8"));
//    }

//    protected  Date parseDate(byte[] bytes) throws UnsupportedEncodingException, ParseException {
//        String data = new String(bytes, "UTF-8");
//        return parseDate(data);
//    }
//
//    protected Date parseDate(String data) throws ParseException {
//        try {
//            Date parse = new SimpleDateFormat(DATE_FORMAT_SMALL).parse(data);
//            return parse;
//        } catch (ParseException e) {
//            try {
//                Date parse = new SimpleDateFormat(DATE_FORMAT_BIG).parse(data);
//                return parse;
//            } catch (ParseException e1) {
//                LOGGER.log(Level.SEVERE,e1.getMessage(),e1);
//            }
//            throw e;
//        }
//    }
}
