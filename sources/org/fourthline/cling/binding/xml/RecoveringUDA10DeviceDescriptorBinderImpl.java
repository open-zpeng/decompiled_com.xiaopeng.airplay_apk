package org.fourthline.cling.binding.xml;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.seamless.xml.ParserException;
import org.xml.sax.SAXParseException;
/* loaded from: classes.dex */
public class RecoveringUDA10DeviceDescriptorBinderImpl extends UDA10DeviceDescriptorBinderImpl {
    private static Logger log = Logger.getLogger(RecoveringUDA10DeviceDescriptorBinderImpl.class.getName());

    /* JADX WARN: Removed duplicated region for block: B:31:0x0091 A[Catch: ValidationException -> 0x0009, TRY_LEAVE, TryCatch #2 {ValidationException -> 0x0009, blocks: (B:4:0x0003, B:9:0x000e, B:13:0x0015, B:15:0x003a, B:20:0x005f, B:23:0x0066, B:31:0x0091, B:34:0x0098, B:38:0x009f, B:39:0x00c1, B:41:0x00cc, B:46:0x00f1, B:45:0x00d3, B:27:0x006d, B:19:0x0041), top: B:57:0x0001, inners: #0, #1, #3, #4, #5 }] */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0066 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:62:0x00cc A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @Override // org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl, org.fourthline.cling.binding.xml.DeviceDescriptorBinder
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public <D extends org.fourthline.cling.model.meta.Device> D describe(D r10, java.lang.String r11) throws org.fourthline.cling.binding.xml.DescriptorBindingException, org.fourthline.cling.model.ValidationException {
        /*
            Method dump skipped, instructions count: 261
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl.describe(org.fourthline.cling.model.meta.Device, java.lang.String):org.fourthline.cling.model.meta.Device");
    }

    private String fixGarbageLeadingChars(String descriptorXml) {
        int index = descriptorXml.indexOf("<?xml");
        return index == -1 ? descriptorXml : descriptorXml.substring(index);
    }

    protected String fixGarbageTrailingChars(String descriptorXml, DescriptorBindingException ex) {
        int index = descriptorXml.indexOf("</root>");
        if (index == -1) {
            log.warning("No closing </root> element in descriptor");
            return null;
        } else if (descriptorXml.length() == "</root>".length() + index) {
            return null;
        } else {
            log.warning("Detected garbage characters after <root> node, removing");
            return descriptorXml.substring(0, index) + "</root>";
        }
    }

    protected String fixMissingNamespaces(String descriptorXml, DescriptorBindingException ex) {
        String message;
        Throwable cause = ex.getCause();
        if (((cause instanceof SAXParseException) || (cause instanceof ParserException)) && (message = cause.getMessage()) != null) {
            Pattern pattern = Pattern.compile("The prefix \"(.*)\" for element");
            Matcher matcher = pattern.matcher(message);
            if (!matcher.find() || matcher.groupCount() != 1) {
                Pattern pattern2 = Pattern.compile("undefined prefix: ([^ ]*)");
                matcher = pattern2.matcher(message);
                if (!matcher.find() || matcher.groupCount() != 1) {
                    return null;
                }
            }
            String missingNS = matcher.group(1);
            Logger logger = log;
            logger.warning("Fixing missing namespace declaration for: " + missingNS);
            Pattern pattern3 = Pattern.compile("<root([^>]*)");
            Matcher matcher2 = pattern3.matcher(descriptorXml);
            if (!matcher2.find() || matcher2.groupCount() != 1) {
                log.fine("Could not find <root> element attributes");
                return null;
            }
            String rootAttributes = matcher2.group(1);
            Logger logger2 = log;
            logger2.fine("Preserving existing <root> element attributes/namespace declarations: " + matcher2.group(0));
            Pattern pattern4 = Pattern.compile("<root[^>]*>(.*)</root>", 32);
            Matcher matcher3 = pattern4.matcher(descriptorXml);
            if (!matcher3.find() || matcher3.groupCount() != 1) {
                log.fine("Could not extract body of <root> element");
                return null;
            }
            String rootBody = matcher3.group(1);
            return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root " + String.format(Locale.ROOT, "xmlns:%s=\"urn:schemas-dlna-org:device-1-0\"", missingNS) + rootAttributes + ">" + rootBody + "</root>";
        }
        return null;
    }

    protected void handleInvalidDescriptor(String xml, DescriptorBindingException exception) throws DescriptorBindingException {
        throw exception;
    }

    protected <D extends Device> D handleInvalidDevice(String xml, D device, ValidationException exception) throws ValidationException {
        throw exception;
    }
}
