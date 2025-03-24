package com.LT.restDummy.helper;

import com.LT.restDummy.exception.ServiceException;
import com.LT.restDummy.file.ServiceFileHandler;
import com.LT.restDummy.domain.model.Service;
import com.LT.restDummy.service.ServiceValue;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ResponseHelperTest {
    private static String simpleXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<SetActivateCardWithoutPlasticRq>\n" +
            "<RqUID>__RqUID__</RqUID>\n" +
            "<RqTm>__RqTm__</RqTm>\n" +
            "<OperUID>68b7eda790174610aa3254a837c45b53</OperUID>\n" +
            "<RegNumber>PLBRNCHUFSCCACT1602202300p1_0000000010519593273</RegNumber>\n" +
            "<CardNum>427901******5965</CardNum>\n" +
            "</SetActivateCardWithoutPlasticRq>";
    private static String thresholdContent = "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 0,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-\n" +
            "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 1,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-\n" +
            "-###-\n" +
            "{\n" +
            "    \"status\": {\n" +
            "        \"statusCode\": 2,\n" +
            "        \"errorCode\": null,\n" +
            "        \"errorMessage\": null\n" +
            "        },\n" +
            "    \"loanList\": []\n" +
            "}\n" +
            "-###-";
    private static String simpleJsonContent = "{\"status\": {\n" +
            "                \"statusCode\": 0,\n" +
            "                \"rqUID\": \"__rqUID__\",\n" +
            "                \"rqTm\": \"__rqTm__\",\n" +
            "                \"errorCode\": null,\n" +
            "                \"errorMessage\": null\n" +
            "            },\n" +
            "            \"loanList\": []\n" +
            "        }";
    private static String jsonRequest = " {\n" +
            "    \"data\": {\n" +
            "        \"getPersonLoanList\": {\n" +
            "            \"system\": {\n" +
            "                \"rqUID\": \"W1307861d3754f89b65ea7e95800b6sc\",\n" +
            "                \"rqTm\": \"2024-02-08T19:08:28.123+03:00\",\n" +
            "                \"operUID\": 514617,\n" +
            "                \"sessionID\": null,\n" +
            "                \"userID\": null,\n" +
            "                \"clientID\": \"514617\",\n" +
            "                \"epkID\": \"1843001558045206194\",\n" +
            "                \"sPName\": \"UCP/cbp-integration-gfl-in-srv\",\n" +
            "                \"sCName\": \"ufs:ccard_issue_branch_mobile\",\n" +
            "                \"channelCode\": \"ВСП\",\n" +
            "                \"blockID\": null,\n" +
            "                \"serviceName\": \"SrvGetFullLoanList\",\n" +
            "                \"serviceVersion\": \"001.301001\"\n" +
            "            },\n" +
            "            \"status\": {\n" +
            "                \"statusCode\": 0,\n" +
            "                \"errorCode\": null,\n" +
            "                \"errorMessage\": null\n" +
            "            },\n" +
            "            \"loanList\": []\n" +
            "        }\n" +
            "    }\n" +
            "}";
    private static String xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<SetActivateCardWithoutPlasticRq>\n" +
            "<RqUID>F1307861d3754f89b65ea7e95800b6fc</RqUID>\n" +
            "<RqTm>2023-02-16T14:48:29.859+03:00</RqTm>\n" +
            "<OperUID>68b7eda790174610aa3254a837c45b53</OperUID>\n" +
            "<SPName>urn:sbrfsystems:99-ufs-fl</SPName>\n" +
            "<SystemId>urn:sbrfsystems:99-tsufx</SystemId>\n" +
            "<RegNumber>PLBRNCHUFSCCACT1602202300p1_0000000010519593273</RegNumber>\n" +
            "<CardNum>427901******5965</CardNum>\n" +
            "<ActivationCode>Y</ActivationCode>\n" +
            "<StatusComment>Activation credit card</StatusComment>\n" +
            "</SetActivateCardWithoutPlasticRq>";

    @BeforeEach
    public void beforeEach() {
        HashMap<String, Service> services = new HashMap<>();
        HashMap<String, String> params1 = new HashMap<>();
        HashMap<String, String> params2 = new HashMap<>();
        HashMap<String, String> params3 = new HashMap<>();
        HashMap<String, String> params4 = new HashMap<>();
        params1.put("type", "json");
        params1.put("timeout", "3000");
        params1.put("delay", "1000");
        params2.put("type", "json");
        params2.put("timeout", "4000");
        params2.put("delay", "2000");
        params2.put("endpoint", "/end/sss/a");
        params2.put("threshold", "[45,15,40]");
        params3.put("type", "xml");
        params3.put("timeout", "5000");
        params3.put("delay", "1000");
        params4.put("type", "json");
        params4.put("timeout", "4000");
        params4.put("delay", "2000");
        params4.put("param.name", "dpan");
        params4.put("param.value", "522860D5254A88433");
        params4.put("param.responseNum", "2");
        services.put("service1", ServiceFileHandler.getService("service1", simpleJsonContent, params1));
        services.put("service2", ServiceFileHandler.getService("service2", thresholdContent, params2));
        services.put("service3", ServiceFileHandler.getService("service3", simpleXmlContent, params3));
        services.put("service4", ServiceFileHandler.getService("service4", thresholdContent, params4));
        ServiceValue.getInstance().initialize(services);
    }

    /**
     * Tests returnResponse()
     */
    @Test
    public void shouldReturnResponse() throws ExecutionException, InterruptedException {
        String response = ResponseHelper.returnResponse(jsonRequest, "service1", 0, null).get().toString();
        Assertions.assertTrue(response.contains("W1307861d3754f89b65ea7e95800b6sc"));
    }

    @Test
    public void shouldReturnResponseAfter4000ms() {
        LocalTime localTimeBefore = LocalTime.now();
        ResponseHelper.returnResponse(jsonRequest, "service1", 4000, null).join();
        LocalTime localTimeAfter = LocalTime.now();
        long delay = localTimeAfter.getSecond() - localTimeBefore.getSecond();
        Assertions.assertTrue(delay >= 4);
    }

    @Test
    public void shouldReturnThrowServiceException() {
        Assert.assertThrows(ServiceException.class, () -> ResponseHelper.returnResponse(jsonRequest, "service1", 0, false));
    }

    /**
     * Tests getHeader()
     */
    @Test
    public void shouldReturnHeaderJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        Assertions.assertEquals(headers, ResponseHelper.getHeader("service1"));
    }

    @Test
    public void shouldReturnHeaderXml() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/xml");
        Assertions.assertEquals(headers, ResponseHelper.getHeader("service3"));
    }

    /**
     * Tests getResponseByPercent()
     */
//    @Test
//    public void shouldGetResponseByPercent() {
//        String threshold45 = "\n{\n" +
//                "    \"status\": {\n" +
//                "        \"statusCode\": 0,\n" +
//                "        \"errorCode\": null,\n" +
//                "        \"errorMessage\": null\n" +
//                "        },\n" +
//                "    \"loanList\": []\n" +
//                "}\n";
//        String threshold60 = "\n{\n" +
//                "    \"status\": {\n" +
//                "        \"statusCode\": 1,\n" +
//                "        \"errorCode\": null,\n" +
//                "        \"errorMessage\": null\n" +
//                "        },\n" +
//                "    \"loanList\": []\n" +
//                "}\n";
//        String threshold100 = "\n{\n" +
//                "    \"status\": {\n" +
//                "        \"statusCode\": 2,\n" +
//                "        \"errorCode\": null,\n" +
//                "        \"errorMessage\": null\n" +
//                "        },\n" +
//                "    \"loanList\": []\n" +
//                "}\n";
//        Assertions.assertEquals(threshold45, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 34));
//        Assertions.assertEquals(threshold45, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 1));
//        Assertions.assertEquals(threshold45, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 45));
//        Assertions.assertEquals(threshold60, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 46));
//        Assertions.assertEquals(threshold60, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 60));
//        Assertions.assertEquals(threshold60, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 55));
//        Assertions.assertEquals(threshold100, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 61));
//        Assertions.assertEquals(threshold100, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 83));
//        Assertions.assertEquals(threshold100, ResponseHelper.getResponseByPercent(ServiceValue.getInstance().getServiceByName("service2"), 100));
//    }

    /**
     * Tests getResponseByPercent()
     */
//    @Test
//    public void shouldGetResponseByParam() {
//        String defaultP = "\n{\n" +
//                "    \"status\": {\n" +
//                "        \"statusCode\": 0,\n" +
//                "        \"errorCode\": null,\n" +
//                "        \"errorMessage\": null\n" +
//                "        },\n" +
//                "    \"loanList\": []\n" +
//                "}\n";
//        String changenableP = "\n{\n" +
//                "    \"status\": {\n" +
//                "        \"statusCode\": 1,\n" +
//                "        \"errorCode\": null,\n" +
//                "        \"errorMessage\": null\n" +
//                "        },\n" +
//                "    \"loanList\": []\n" +
//                "}\n";
//        String request = "{\n" +
//                "  \"clientId\": \"1349441611596980486\",\n" +
//                "  \"mainPaymentTool\": {\n" +
//                "    \"dpan\": \"d\",\n" +
//                "    \"type\": \"CARD\"\n" +
//                "  }\n" +
//                "}";
//
//        String request2 = "{\n" +
//                "  \"clientId\": \"1349441611596980486\",\n" +
//                "  \"mainPaymentTool\": {\n" +
//                "    \"dpan\": \"522860D5254A88433\",\n" +
//                "    \"type\": \"CARD\"\n" +
//                "  }\n" +
//                "}";
//        Assertions.assertEquals(defaultP, ResponseHelper.getResponseByParam(ServiceValue.getInstance().getServiceByName("service4"), request));
//        Assertions.assertEquals(changenableP, ResponseHelper.getResponseByParam(ServiceValue.getInstance().getServiceByName("service4"), request2));
//    }

    /**
     * Tests parameterCorrelate()
     */
    @Test
    public void shouldReturnParameterCorrelateJsonXmlSimple() {
        Assertions.assertEquals("W1307861d3754f89b65ea7e95800b6sc", ResponseHelper.parameterCorrelate(jsonRequest, "rqUID", "json"));
        Assertions.assertEquals("F1307861d3754f89b65ea7e95800b6fc", ResponseHelper.parameterCorrelate(xmlRequest, "RqUID", "xml"));
    }

//     TODO fix
//    @Test
//    public void shouldReturnParameterCorrelateJsonXmlLowCase() {
//Assertions.assertEquals("W1307861d3754f89b65ea7e95800b6sc", ResponseHelper.parameterCorrelate(jsonRequest, "rquid", "json"));
////        Assertions.assertEquals("F1307861d3754f89b65ea7e95800b6fc", ResponseHelper.parameterCorrelate(xmlRequest, "rquid", "xml"));
////    }

    @Test
    public void shouldReturnErrorNonexistentParameterCorrelateJsonXml() {
        Assertions.assertEquals("Значение не найдено в запросе", ResponseHelper.parameterCorrelate(jsonRequest, "www", "json"));
        Assertions.assertEquals("Значение не найдено в запросе", ResponseHelper.parameterCorrelate(xmlRequest, "ddd", "xml"));
    }

    @Test
    public void shouldReturnErrorNonexistentTypeParameterCorrelateJsonXml() {
        Assertions.assertEquals("У вас не указан type для сервиса или type не поддерживается", ResponseHelper.parameterCorrelate(jsonRequest, "rqUID", "jesone"));
    }

    @Test
    public void shouldReturnErrorXmlRequestForTypeJsonParameterCorrelate() {
        Assertions.assertEquals("Проверьте соответствие type и входящего запроса. Заглушка не может распарсить входящий запрос как json.", ResponseHelper.parameterCorrelate(xmlRequest, "rqUID", "json"));
    }

    @Test
    public void shouldReturnErrorJsonRequestForTypeXmlParameterCorrelate() {
        Assertions.assertEquals("Значение не найдено в запросе", ResponseHelper.parameterCorrelate(jsonRequest, "RqUID", "xml"));
    }

    /**
     * Tests "random" methods
     */
    @Test
    public void shouldReturnRandomRqUID() {
        String rqUid = ResponseHelper.randomRqUID(34);
        Assertions.assertTrue(rqUid.matches("(?=.*[A-F]+)(?=.*[a-f]+)(?=.*[0-9]+).{34}"));
        Assertions.assertEquals(34, rqUid.length());
    }

    @Test
    public void shouldReturnRandomNumberAndChar() {
        String rqUid = ResponseHelper.randomRqUID(36);
        Assertions.assertTrue(rqUid.matches("(?=.*[A-Z]+)(?=.*[a-z]+)(?=.*[0-9]+).{36}"));
        Assertions.assertEquals(36, rqUid.length());
    }

    @Test
    public void shouldReturnRandomNumber() {
        String rqUid = ResponseHelper.randomRqUID(23);
        Assertions.assertTrue(rqUid.matches("(?=.*[0-9]+).{23}"));
        Assertions.assertEquals(23, rqUid.length());
    }

    @Test
    public void shouldReturnRandomChar() {
        String rqUid = ResponseHelper.randomRqUID(40);
        Assertions.assertTrue(rqUid.matches("(?=.*[A-Z]+)(?=.*[a-z]+).{40}"));
        Assertions.assertEquals(40, rqUid.length());
    }

    /**
     * Tests responseCorrelate()
     */
    @Test
    public void shouldReturnResponseCorrelate() {
        String response = ResponseHelper.responseCorrelate(jsonRequest, simpleJsonContent, "json");
        LocalDate localDateTime = LocalDate.now();
        Assertions.assertTrue(response.contains("W1307861d3754f89b65ea7e95800b6sc"));
        Assertions.assertTrue(response.contains(localDateTime.toString()));
    }
}