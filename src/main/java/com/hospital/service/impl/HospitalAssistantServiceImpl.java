package com.hospital.service.impl;

import com.hospital.dto.AssistantResponse;
import com.hospital.entity.ChatbotConversationLog;
import com.hospital.entity.DepartmentInfo;
import com.hospital.entity.MedicalServicePrice;
import com.hospital.entity.MedicalSupply;
import com.hospital.repository.ChatbotConversationLogRepository;
import com.hospital.repository.DepartmentInfoRepository;
import com.hospital.repository.MedicalServicePriceRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.service.GeminiAssistantService;
import com.hospital.service.HospitalAssistantService;
import com.hospital.service.OpenAiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HospitalAssistantServiceImpl implements HospitalAssistantService {

    private static final Pattern SUPPLY_CODE_PATTERN = Pattern.compile("\\b[a-z]{1,5}\\d{2,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Charset WINDOWS_1252 = Charset.forName("Windows-1252");
    private static final String HOSPITAL_HOURS = "Bá»‡nh viá»‡n khÃ¡m tá»« 7h00 Ä‘áº¿n 17h00 tá»« thá»© 2 Ä‘áº¿n thá»© 7. Khoa Cáº¥p cá»©u hoáº¡t Ä‘á»™ng 24/7.";
    private static final String HOSPITAL_ADDRESS = "Äá»‹a chá»‰ bá»‡nh viá»‡n: 123 Nguyá»…n VÄƒn Linh.";
    private static final String HOSPITAL_HOTLINE = "Hotline há»— trá»£: 1900 1234.";
    private static final String DEPARTMENT_LIST = "CÃ¡c khoa hiá»‡n cÃ³: Ná»™i tá»•ng quÃ¡t, Tim máº¡ch, TiÃªu hÃ³a, HÃ´ háº¥p, Tháº§n kinh, Da liá»…u, Tai mÅ©i há»ng, CÆ¡ xÆ°Æ¡ng khá»›p, Nhi khoa, Sáº£n phá»¥ khoa.";
    private static final String MEDICAL_DISCLAIMER = "Luu y: thong tin nay chi ho tro sang loc so bo, khong thay the chan doan hoac chi dinh dieu tri cua bac si.";
    private static final int INVENTORY_RESULT_LIMIT = 5;

    private final MedicalSupplyRepository medicalSupplyRepository;
    private final DepartmentInfoRepository departmentInfoRepository;
    private final MedicalServicePriceRepository medicalServicePriceRepository;
    private final ChatbotConversationLogRepository chatbotConversationLogRepository;
    private final GeminiAssistantService geminiAssistantService;
    private final OpenAiAssistantService openAiAssistantService;

    @Override
    public AssistantResponse analyze(String message, boolean authenticated) {
        AssistantResponse response = sanitizeResponse(analyzeInternal(message, authenticated));
        persistConversation(message, authenticated, "WEB", response);
        return response;
    }

    private AssistantResponse analyzeInternal(String message, boolean authenticated) {
        String rawMessage = message == null ? "" : message.trim();
        if (rawMessage.isBlank()) {
            return new AssistantResponse(
                    "FAQ",
                    "",
                    0,
                    "nh\u1eb9",
                    "Vui l\u00f2ng nh\u1eadp c\u00e2u h\u1ecfi ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 t\u00f4i h\u1ed7 tr\u1ee3.",
                    authenticated
                            ? "B\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi v\u1ec1 gi\u1edd kh\u00e1m, \u0111\u1ecba ch\u1ec9, hotline, khoa kh\u00e1m, v\u1eadt t\u01b0 s\u1eafp h\u1ebft h\u1ea1n ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng."
                            : "B\u1ea1n c\u00f3 th\u1ec3 h\u1ecfi v\u1ec1 gi\u1edd kh\u00e1m, \u0111\u1ecba ch\u1ec9, hotline ho\u1eb7c m\u00f4 t\u1ea3 tri\u1ec7u ch\u1ee9ng \u0111\u1ec3 \u0111\u01b0\u1ee3c s\u00e0ng l\u1ecdc s\u01a1 b\u1ed9.",
                    false
            );
        }

        String normalized = normalize(rawMessage);

        boolean asksHours = containsAny(normalized, "gio kham", "mo cua", "lam viec", "may gio", "gio nao");
        boolean asksAddress = containsAny(normalized, "dia chi", "o dau", "duong nao");
        boolean asksHotline = containsAny(normalized, "hotline", "so dien thoai", "so hotline", "lien he");
        boolean asksDepartments = containsAny(normalized, "danh sach khoa", "co khoa nao", "nhung khoa nao", "khoa kham", "chuyen khoa");
        boolean asksPrice = containsAny(normalized, "chi phi", "gia kham", "vien phi", "bao nhieu tien", "phi kham", "bang gia", "gia dich vu");

        MedicalIntent intent = detectMedicalIntent(normalized);
        boolean hasSymptom = intent.hasAnySymptom();

        if (authenticated && !hasSymptom && isInventoryIntent(normalized)) {
            return handleInventoryRequest(rawMessage, normalized);
        }

        if (!authenticated && !hasSymptom && isInventoryIntent(normalized)) {
            return new AssistantResponse(
                    "FAQ",
                    "",
                    0,
                    "nháº¹",
                    "YÃªu cáº§u nÃ y liÃªn quan Ä‘áº¿n dá»¯ liá»‡u kho ná»™i bá»™ nÃªn tÃ´i chÆ°a thá»ƒ tráº£ lá»i á»Ÿ cháº¿ Ä‘á»™ cÃ´ng khai.",
                    "HÃ£y Ä‘Äƒng nháº­p Ä‘á»ƒ há»i vá» tá»“n kho, háº¡n dÃ¹ng, mÃ£ váº­t tÆ° hoáº·c tráº¡ng thÃ¡i cáº£nh bÃ¡o trong há»‡ thá»‘ng.",
                    false
            );
        }

        if (!hasSymptom && (asksHours || asksAddress || asksHotline || asksDepartments || asksPrice)) {
            return buildFaqResponse(asksHours, asksAddress, asksHotline, asksDepartments, asksPrice, authenticated);
        }

        if (!hasSymptom) {
            return new AssistantResponse(
                    "FAQ",
                    "",
                    0,
                    "nháº¹",
                    authenticated
                            ? "TÃ´i cÃ³ thá»ƒ há»— trá»£ thÃ´ng tin bá»‡nh viá»‡n, sÃ ng lá»c triá»‡u chá»©ng sÆ¡ bá»™ vÃ  tra cá»©u nhanh dá»¯ liá»‡u kho váº­t tÆ°."
                            : "TÃ´i cÃ³ thá»ƒ há»— trá»£ thÃ´ng tin bá»‡nh viá»‡n vÃ  sÃ ng lá»c triá»‡u chá»©ng sÆ¡ bá»™.",
                    "Báº¡n hÃ£y mÃ´ táº£ rÃµ triá»‡u chá»©ng nhÆ° Ä‘au ngá»±c, khÃ³ thá»Ÿ, Ä‘au bá»¥ng, Ä‘au tay Ä‘au chÃ¢n, ná»•i máº©n hoáº·c há»i vá» giá» khÃ¡m, Ä‘á»‹a chá»‰, hotline.",
                    false
            );
        }

        return buildMedicalTriageResponse(rawMessage, asksHours, asksAddress, asksHotline, asksDepartments, asksPrice, intent);
    }

    private AssistantResponse handleInventoryRequest(String rawMessage, String normalized) {
        if (containsAny(normalized, "sap het han", "gan het han", "het han som", "han su dung", "het han")) {
            return buildExpiringSuppliesResponse();
        }
        if (containsAny(normalized, "duoi muc ton", "ton thap", "sap het", "het hang", "thieu hang", "canh bao ton")) {
            return buildLowStockResponse();
        }
        if (containsAny(normalized, "tong quan kho", "tong quan vat tu", "tom tat kho", "thong ke kho", "bao cao nhanh")) {
            return buildInventoryOverviewResponse();
        }

        Optional<String> supplyCode = extractSupplyCode(rawMessage);
        if (supplyCode.isPresent()) {
            return buildSupplyLookupResponse(supplyCode.get());
        }

        if (containsAny(normalized, "tra cuu", "tim vat tu", "vat tu", "thuoc", "ma vat tu", "qr")) {
            return buildSupplySearchResponse(normalized);
        }

        return buildInventoryOverviewResponse();
    }

    private AssistantResponse buildInventoryOverviewResponse() {
        LocalDate threshold = LocalDate.now().plusDays(30);
        long totalSupplies = medicalSupplyRepository.count();
        long lowStockCount = medicalSupplyRepository.findLowStockSupplies().size();
        long expiringCount = medicalSupplyRepository.findExpiringSupplies(threshold).size();

        String answer = String.format(
                "Tá»•ng quan kho hiá»‡n cÃ³ %d váº­t tÆ°. Trong Ä‘Ã³ cÃ³ %d váº­t tÆ° dÆ°á»›i má»©c tá»“n vÃ  %d váº­t tÆ° sáº¯p háº¿t háº¡n trong 30 ngÃ y tá»›i.",
                totalSupplies, lowStockCount, expiringCount
        );
        String advice = "Báº¡n cÃ³ thá»ƒ há»i tiáº¿p: váº­t tÆ° nÃ o sáº¯p háº¿t háº¡n, váº­t tÆ° nÃ o dÆ°á»›i má»©c tá»“n, hoáº·c tra cá»©u mÃ£ nhÆ° VT001.";
        return new AssistantResponse("INVENTORY", "Kho váº­t tÆ°", 0, "thÃ´ng tin", answer, advice, false);
    }

    private AssistantResponse buildLowStockResponse() {
        List<MedicalSupply> lowStockSupplies = medicalSupplyRepository.findLowStockSupplies();
        if (lowStockSupplies.isEmpty()) {
            return new AssistantResponse(
                    "INVENTORY",
                    "Kho váº­t tÆ°",
                    0,
                    "á»•n Ä‘á»‹nh",
                    "Hiá»‡n chÆ°a cÃ³ váº­t tÆ° nÃ o dÆ°á»›i má»©c tá»“n tá»‘i thiá»ƒu.",
                    "Báº¡n cÃ³ thá»ƒ há»i thÃªm vá» váº­t tÆ° sáº¯p háº¿t háº¡n hoáº·c tra cá»©u theo mÃ£ váº­t tÆ°.",
                    false
            );
        }

        List<String> lines = lowStockSupplies.stream()
                .limit(INVENTORY_RESULT_LIMIT)
                .map(item -> String.format("%s (%s): cÃ²n %d %s, má»©c tá»‘i thiá»ƒu %d",
                        item.getName(), item.getCode(), item.getQuantity(), item.getUnit(), item.getMinimumStock()))
                .toList();

        String answer = "CÃ¡c váº­t tÆ° Ä‘ang dÆ°á»›i má»©c tá»“n: " + String.join("; ", lines) + ".";
        String advice = lowStockSupplies.size() > INVENTORY_RESULT_LIMIT
                ? "Danh sÃ¡ch cÃ²n dÃ i hÆ¡n. Báº¡n nÃªn má»Ÿ mÃ n hÃ¬nh tá»“n kho Ä‘á»ƒ xá»­ lÃ½ Ä‘áº§y Ä‘á»§ cÃ¡c má»¥c cáº£nh bÃ¡o."
                : "Báº¡n nÃªn Æ°u tiÃªn nháº­p bá»• sung cÃ¡c váº­t tÆ° nÃ y trÆ°á»›c khi phÃ¡t sinh phiáº¿u xuáº¥t tiáº¿p theo.";
        return new AssistantResponse("INVENTORY", "Kho váº­t tÆ°", 0, "cáº£nh bÃ¡o", answer, advice, false);
    }

    private AssistantResponse buildExpiringSuppliesResponse() {
        LocalDate threshold = LocalDate.now().plusDays(30);
        List<MedicalSupply> expiringSupplies = medicalSupplyRepository.findExpiringSupplies(threshold);
        if (expiringSupplies.isEmpty()) {
            return new AssistantResponse(
                    "INVENTORY",
                    "Kho váº­t tÆ°",
                    0,
                    "á»•n Ä‘á»‹nh",
                    "Hiá»‡n chÆ°a cÃ³ váº­t tÆ° nÃ o sáº¯p háº¿t háº¡n trong 30 ngÃ y tá»›i.",
                    "Báº¡n cÃ³ thá»ƒ há»i thÃªm vá» cáº£nh bÃ¡o tá»“n kho hoáº·c tra cá»©u chi tiáº¿t theo mÃ£ váº­t tÆ°.",
                    false
            );
        }

        List<String> lines = expiringSupplies.stream()
                .limit(INVENTORY_RESULT_LIMIT)
                .map(item -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), item.getExpiryDate());
                    return String.format("%s (%s): háº¿t háº¡n %s, cÃ²n %d ngÃ y",
                            item.getName(), item.getCode(), item.getExpiryDate(), Math.max(days, 0));
                })
                .toList();

        String answer = "CÃ¡c váº­t tÆ° sáº¯p háº¿t háº¡n: " + String.join("; ", lines) + ".";
        String advice = expiringSupplies.size() > INVENTORY_RESULT_LIMIT
                ? "Danh sÃ¡ch cÃ²n nhiá»u hÆ¡n. Báº¡n nÃªn kiá»ƒm tra dashboard hoáº·c mÃ n hÃ¬nh váº­t tÆ° Ä‘á»ƒ xá»­ lÃ½ toÃ n bá»™ cÃ¡c lÃ´ rá»§i ro."
                : "Báº¡n nÃªn Æ°u tiÃªn xuáº¥t dÃ¹ng theo FEFO hoáº·c láº­p phÆ°Æ¡ng Ã¡n xá»­ lÃ½ cÃ¡c má»¥c gáº§n háº¿t háº¡n.";
        return new AssistantResponse("INVENTORY", "Kho váº­t tÆ°", 0, "cáº£nh bÃ¡o", answer, advice, false);
    }

    private AssistantResponse buildSupplyLookupResponse(String supplyCode) {
        Optional<MedicalSupply> supplyOptional = medicalSupplyRepository.findByCode(supplyCode.toUpperCase(Locale.ROOT));
        if (supplyOptional.isEmpty()) {
            return new AssistantResponse(
                    "INVENTORY",
                    "Kho váº­t tÆ°",
                    0,
                    "khÃ´ng tháº¥y",
                    "TÃ´i khÃ´ng tÃ¬m tháº¥y váº­t tÆ° cÃ³ mÃ£ " + supplyCode.toUpperCase(Locale.ROOT) + ".",
                    "HÃ£y kiá»ƒm tra láº¡i mÃ£ váº­t tÆ° hoáº·c há»i theo tÃªn gáº§n Ä‘Ãºng Ä‘á»ƒ tÃ´i tÃ¬m thÃªm.",
                    false
            );
        }

        MedicalSupply supply = supplyOptional.get();
        String answer = String.format(
                "%s (%s) hiá»‡n cÃ²n %d %s, má»©c tá»‘i thiá»ƒu %d, háº¡n dÃ¹ng %s, tráº¡ng thÃ¡i %s.",
                supply.getName(),
                supply.getCode(),
                supply.getQuantity(),
                supply.getUnit(),
                supply.getMinimumStock(),
                supply.getExpiryDate(),
                formatStatus(supply)
        );
        String advice = "Náº¿u cáº§n, báº¡n cÃ³ thá»ƒ há»i tiáº¿p váº­t tÆ° nÃ o sáº¯p háº¿t háº¡n hoáº·c cÃ¡c má»¥c Ä‘ang dÆ°á»›i má»©c tá»“n.";
        return new AssistantResponse("INVENTORY", "Kho váº­t tÆ°", 0, "chi tiáº¿t", answer, advice, false);
    }

    private AssistantResponse buildSupplySearchResponse(String normalized) {
        String keyword = extractSearchKeyword(normalized);
        if (keyword.isBlank()) {
            return buildInventoryOverviewResponse();
        }

        List<MedicalSupply> matches = medicalSupplyRepository.findAll().stream()
                .filter(item -> containsNormalized(item.getCode(), keyword) || containsNormalized(item.getName(), keyword))
                .sorted(Comparator.comparing(MedicalSupply::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(INVENTORY_RESULT_LIMIT)
                .toList();

        if (matches.isEmpty()) {
            return new AssistantResponse(
                    "INVENTORY",
                    "Kho váº­t tÆ°",
                    0,
                    "khÃ´ng tháº¥y",
                    "TÃ´i chÆ°a tÃ¬m tháº¥y váº­t tÆ° phÃ¹ há»£p vá»›i tá»« khÃ³a \"" + keyword + "\".",
                    "Báº¡n hÃ£y thá»­ nháº­p mÃ£ váº­t tÆ° nhÆ° VT001 hoáº·c tÃªn rÃµ hÆ¡n Ä‘á»ƒ tÃ´i tra cá»©u láº¡i.",
                    false
            );
        }

        String answer = "Káº¿t quáº£ tra cá»©u: " + matches.stream()
                .map(item -> String.format("%s (%s) - cÃ²n %d %s", item.getName(), item.getCode(), item.getQuantity(), item.getUnit()))
                .reduce((left, right) -> left + "; " + right)
                .orElse("") + ".";
        String advice = "Báº¡n cÃ³ thá»ƒ há»i cá»¥ thá»ƒ hÆ¡n theo mÃ£ váº­t tÆ° Ä‘á»ƒ xem háº¡n dÃ¹ng, má»©c tá»“n tá»‘i thiá»ƒu vÃ  tráº¡ng thÃ¡i cáº£nh bÃ¡o.";
        return new AssistantResponse("INVENTORY", "Kho váº­t tÆ°", 0, "thÃ´ng tin", answer, advice, false);
    }

    private AssistantResponse buildFaqResponse(boolean asksHours, boolean asksAddress, boolean asksHotline,
                                               boolean asksDepartments, boolean asksPrice, boolean authenticated) {
        StringBuilder answer = new StringBuilder("ThÃ´ng tin bá»‡nh viá»‡n:");
        appendFaqInfo(answer, asksHours, asksAddress, asksHotline, asksDepartments, asksPrice);
        String advice = authenticated
                ? "Náº¿u cáº§n, báº¡n cÅ©ng cÃ³ thá»ƒ há»i vá» dá»¯ liá»‡u kho nhÆ° váº­t tÆ° sáº¯p háº¿t háº¡n hoáº·c tra cá»©u mÃ£ váº­t tÆ°."
                : "Náº¿u báº¡n cÃ³ triá»‡u chá»©ng cá»¥ thá»ƒ, hÃ£y mÃ´ táº£ Ä‘á»ƒ tÃ´i gá»£i Ã½ khoa khÃ¡m vÃ  má»©c Ä‘á»™ Æ°u tiÃªn.";
        return new AssistantResponse("FAQ", "", 0, "nháº¹", answer.toString(), advice, false);
    }

    private AssistantResponse buildMedicalTriageResponse(String rawMessage, boolean asksHours, boolean asksAddress, boolean asksHotline,
                                                         boolean asksDepartments, boolean asksPrice, MedicalIntent intent) {
        String department = suggestDepartment(intent);
        int riskScore = calculateRiskScore(intent);
        boolean emergency = isEmergencyIntent(intent) || riskScore >= 7;
        String riskLevel = emergency ? "nguy hiá»ƒm" : resolveRiskLevel(riskScore);
        String type = emergency ? "EMERGENCY" : "TRIAGE";

        List<String> symptoms = buildSymptomLabels(intent);
        String symptomText = String.join(", ", symptoms);
        String specialty = specialtyLabel(intent);
        String followUp = buildFollowUpQuestion(intent, emergency);

        StringBuilder answer = new StringBuilder();
        answer.append("NhÃ³m bá»‡nh lÃ½ nghi ngá»: ").append(specialty).append(". ");
        answer.append("Má»©c Ä‘á»™ hiá»‡n táº¡i: ").append(severityLabel(riskLevel)).append(". ");
        if (emergency) {
            answer.append("Báº¡n cÃ³ triá»‡u chá»©ng ").append(symptomText)
                    .append(", Ä‘Ã¢y lÃ  dáº¥u hiá»‡u nguy hiá»ƒm cáº§n Ä‘Æ°á»£c xá»­ lÃ½ kháº©n cáº¥p.");
        } else {
            answer.append("Triá»‡u chá»©ng ").append(symptomText)
                    .append(" hiá»‡n phÃ¹ há»£p Ä‘á»ƒ Ä‘Æ°á»£c sÃ ng lá»c sÆ¡ bá»™.");
        }
        answer.append(" Khoa Ä‘á»‹nh hÆ°á»›ng: ").append(department).append(". ");
        appendDepartmentInfo(answer, department);
        if (hasRedFlag(intent)) {
            answer.append("CÃ³ dáº¥u hiá»‡u cáº£nh bÃ¡o Ä‘á» cáº§n Æ°u tiÃªn khÃ¡m sá»›m. ");
        }
        answer.append("Cáº§n lÃ m rÃµ thÃªm: ").append(followUp).append(".");
        appendFaqInfo(answer, asksHours, asksAddress, asksHotline, asksDepartments, asksPrice);

        String advice;
        if (emergency) {
            advice = "Báº¡n nÃªn Ä‘áº¿n khoa Cáº¥p cá»©u ngay hoáº·c gá»i ngÆ°á»i nhÃ  há»— trá»£ Ä‘Æ°a Ä‘i. KhÃ´ng tá»± lÃ¡i xe náº¿u Ä‘ang khÃ³ thá»Ÿ, Ä‘au ngá»±c tÄƒng lÃªn, yáº¿u tay chÃ¢n Ä‘á»™t ngá»™t, co giáº­t hoáº·c máº¥t Ã½ thá»©c. " + HOSPITAL_HOTLINE;
        } else if ("trung bÃ¬nh".equals(riskLevel)) {
            advice = "Báº¡n nÃªn Ä‘i khÃ¡m trong ngÃ y hoáº·c sá»›m nháº¥t cÃ³ thá»ƒ Ä‘á»ƒ Ä‘Æ°á»£c bÃ¡c sÄ© Ä‘Ã¡nh giÃ¡ trá»±c tiáº¿p. Náº¿u triá»‡u chá»©ng tÄƒng nhanh, xuáº¥t hiá»‡n khÃ³ thá»Ÿ, sá»‘t cao kÃ©o dÃ i, yáº¿u tay chÃ¢n, nÃ³i khÃ³ hoáº·c Ä‘au dá»¯ dá»™i thÃ¬ cáº§n nÃ¢ng má»©c Æ°u tiÃªn ngay. " + HOSPITAL_HOURS;
        } else {
            advice = "Hiá»‡n chÆ°a ghi nháº­n dáº¥u hiá»‡u cáº¥p cá»©u rÃµ, nhÆ°ng báº¡n váº«n nÃªn theo dÃµi sÃ¡t vÃ  Ä‘i khÃ¡m náº¿u triá»‡u chá»©ng kÃ©o dÃ i, tÃ¡i phÃ¡t hoáº·c náº·ng lÃªn. " + HOSPITAL_HOURS;
        }

        AssistantResponse response = new AssistantResponse(type, department, Math.min(riskScore, 10), riskLevel, answer.toString(), advice, emergency);
        return enrichWithGemini(rawMessage, withDisclaimer(response));
    }

    private String specialtyLabel(MedicalIntent intent) {
        if (intent.chestPain || intent.palpitations || intent.bloodPressureIssue || intent.vagueCardiacConcern) {
            return "tim máº¡ch";
        }
        if (intent.weaknessOrNumbness || intent.slurredSpeech || intent.facialDroop || intent.headache) {
            return "tháº§n kinh";
        }
        if (intent.shortBreath || intent.cough || intent.soreThroat || intent.runnyNose) {
            return "hÃ´ háº¥p";
        }
        if (intent.limbPain || intent.jointPain) {
            return "cÆ¡ xÆ°Æ¡ng khá»›p hoáº·c tháº§n kinh ngoáº¡i biÃªn";
        }
        if (intent.stomachPain || intent.diarrhea || intent.vomiting || intent.dehydration) {
            return "tiÃªu hÃ³a";
        }
        if (intent.urinaryIssue) {
            return "ná»™i tá»•ng quÃ¡t hoáº·c tiáº¿t niá»‡u";
        }
        if (intent.diabetesIssue) {
            return "ná»™i tiáº¿t";
        }
        if (intent.rash || intent.skinDisease) {
            return "da liá»…u";
        }
        return "ná»™i tá»•ng quÃ¡t";
    }

    private String buildFollowUpQuestion(MedicalIntent intent, boolean emergency) {
        if (emergency && (intent.facialDroop || intent.slurredSpeech || intent.weaknessOrNumbness)) {
            return "Yáº¿u tay chÃ¢n, mÃ©o miá»‡ng hoáº·c nÃ³i khÃ³ xuáº¥t hiá»‡n tá»« khi nÃ o, cÃ³ xáº£y ra Ä‘á»™t ngá»™t khÃ´ng";
        }
        if (intent.chestPain || intent.palpitations || intent.bloodPressureIssue || intent.vagueCardiacConcern) {
            return "CÆ¡n Ä‘au ngá»±c hay há»“i há»™p kÃ©o dÃ i bao lÃ¢u, cÃ³ lan tay trÃ¡i hoáº·c kÃ¨m khÃ³ thá»Ÿ, vÃ£ má»“ hÃ´i khÃ´ng";
        }
        if (intent.limbPain || intent.jointPain) {
            return "Äau á»Ÿ tay, chÃ¢n hay khá»›p nÃ o, cÃ³ sÆ°ng nÃ³ng Ä‘á», tÃª bÃ¬, yáº¿u cÆ¡ hoáº·c tiá»n sá»­ va cháº¡m cháº¥n thÆ°Æ¡ng khÃ´ng";
        }
        if (intent.headache || intent.facialDroop || intent.slurredSpeech || intent.weaknessOrNumbness) {
            return "CÃ³ yáº¿u má»™t bÃªn ngÆ°á»i, nhÃ¬n má», nÃ³i khÃ³ hoáº·c Ä‘au Ä‘áº§u dá»¯ dá»™i khá»Ÿi phÃ¡t Ä‘á»™t ngá»™t khÃ´ng";
        }
        if (intent.shortBreath || intent.cough || intent.soreThroat || intent.runnyNose) {
            return "Báº¡n ho bao lÃ¢u rá»“i, cÃ³ sá»‘t, Ä‘au ngá»±c, khÃ² khÃ¨ hay khÃ³ thá»Ÿ tÄƒng dáº§n khÃ´ng";
        }
        if (intent.stomachPain || intent.diarrhea || intent.vomiting || intent.dehydration) {
            return "Äau bá»¥ng á»Ÿ vá»‹ trÃ­ nÃ o, nÃ´n hoáº·c Ä‘i ngoÃ i bao nhiÃªu láº§n, cÃ³ khÃ¡t nhiá»u hay tiá»ƒu Ã­t khÃ´ng";
        }
        if (intent.urinaryIssue) {
            return "CÃ³ sá»‘t, Ä‘au hÃ´ng lÆ°ng, tiá»ƒu ra mÃ¡u hoáº·c tiá»ƒu buá»‘t kÃ©o dÃ i nhiá»u ngÃ y khÃ´ng";
        }
        if (intent.diabetesIssue) {
            return "CÃ³ khÃ¡t nÆ°á»›c nhiá»u, sá»¥t cÃ¢n, má»‡t láº£, nhÃ¬n má» hoáº·c tá»«ng Ä‘Æ°á»£c cháº©n Ä‘oÃ¡n Ä‘Ã¡i thÃ¡o Ä‘Æ°á»ng chÆ°a";
        }
        return "Triá»‡u chá»©ng báº¯t Ä‘áº§u tá»« khi nÃ o, cÃ³ náº·ng lÃªn nhanh hoáº·c kÃ¨m sá»‘t, khÃ³ thá»Ÿ, Ä‘au tÄƒng dáº§n khÃ´ng";
    }

    private String severityLabel(String riskLevel) {
        return switch (riskLevel) {
            case "nguy hiá»ƒm" -> "náº·ng, cáº§n cáº¥p cá»©u";
            case "trung bÃ¬nh" -> "cáº§n Ä‘i khÃ¡m sá»›m";
            default -> "nháº¹ hoáº·c chÆ°a cÃ³ dáº¥u hiá»‡u cáº¥p cá»©u rÃµ";
        };
    }

    private List<String> buildSymptomLabels(MedicalIntent intent) {
        List<String> symptoms = new ArrayList<>();
        if (intent.chestPain) symptoms.add("Ä‘au ngá»±c");
        if (intent.vagueCardiacConcern && !intent.chestPain) symptoms.add("khÃ³ chá»‹u vÃ¹ng tim chÆ°a mÃ´ táº£ rÃµ");
        if (intent.palpitations) symptoms.add("há»“i há»™p hoáº·c tim Ä‘áº­p nhanh");
        if (intent.shortBreath) symptoms.add("khÃ³ thá»Ÿ");
        if (intent.bloodPressureIssue) symptoms.add("báº¥t thÆ°á»ng huyáº¿t Ã¡p");
        if (intent.limbPain) symptoms.add("Ä‘au tay chÃ¢n");
        if (intent.jointPain) symptoms.add("Ä‘au xÆ°Æ¡ng khá»›p");
        if (intent.stomachPain) symptoms.add("Ä‘au bá»¥ng hoáº·c Ä‘au dáº¡ dÃ y");
        if (intent.headache) symptoms.add("Ä‘au Ä‘áº§u hoáº·c chÃ³ng máº·t");
        if (intent.weaknessOrNumbness) symptoms.add("tÃª yáº¿u tay chÃ¢n");
        if (intent.slurredSpeech) symptoms.add("nÃ³i khÃ³");
        if (intent.facialDroop) symptoms.add("mÃ©o miá»‡ng hoáº·c liá»‡t máº·t");
        if (intent.cough) symptoms.add("ho");
        if (intent.rash) symptoms.add("ná»•i máº©n hoáº·c phÃ¡t ban");
        if (intent.highFever) symptoms.add("sá»‘t cao");
        if (intent.seizure) symptoms.add("co giáº­t");
        if (intent.unconscious) symptoms.add("máº¥t Ã½ thá»©c");
        if (intent.heavyBleeding) symptoms.add("cháº£y mÃ¡u nhiá»u");
        if (intent.bleedingSpots) symptoms.add("xuáº¥t huyáº¿t hoáº·c cháº¥m Ä‘á» dÆ°á»›i da");
        if (intent.dehydration) symptoms.add("dáº¥u hiá»‡u máº¥t nÆ°á»›c");
        if (intent.child) symptoms.add("triá»‡u chá»©ng á»Ÿ tráº» em");
        if (intent.obgyn) symptoms.add("liÃªn quan thai ká»³ hoáº·c phá»¥ khoa");
        if (intent.soreThroat) symptoms.add("Ä‘au há»ng");
        if (intent.runnyNose) symptoms.add("sá»• mÅ©i hoáº·c ngháº¹t mÅ©i");
        if (intent.diarrhea) symptoms.add("tiÃªu cháº£y");
        if (intent.vomiting) symptoms.add("buá»“n nÃ´n hoáº·c nÃ´n");
        if (intent.toothPain) symptoms.add("Ä‘au rÄƒng hoáº·c viÃªm lá»£i");
        if (intent.earPain) symptoms.add("Ä‘au tai");
        if (intent.eyePain) symptoms.add("Ä‘á» máº¯t hoáº·c cá»™m máº¯t");
        if (intent.urinaryIssue) symptoms.add("tiá»ƒu buá»‘t hoáº·c tiá»ƒu rÃ¡t");
        if (intent.diabetesIssue) symptoms.add("dáº¥u hiá»‡u liÃªn quan Ä‘Æ°á»ng huyáº¿t");
        if (intent.infectiousDisease) symptoms.add("nghi bá»‡nh truyá»n nhiá»…m");
        if (intent.skinDisease) symptoms.add("bá»‡nh lÃ½ da liá»…u");
        return symptoms;
    }

    private void appendFaqInfo(StringBuilder target, boolean asksHours, boolean asksAddress, boolean asksHotline, boolean asksDepartments, boolean asksPrice) {
        if (asksHours) target.append(" ").append(HOSPITAL_HOURS);
        if (asksAddress) target.append(" ").append(HOSPITAL_ADDRESS);
        if (asksHotline) target.append(" ").append(HOSPITAL_HOTLINE);
        if (asksDepartments) target.append(" ").append(DEPARTMENT_LIST);
        if (asksPrice) target.append(" ").append(buildPriceInfo());
    }

    private String buildPriceInfo() {
        List<MedicalServicePrice> prices = medicalServicePriceRepository.findAll();
        if (prices.isEmpty()) {
            return "Chi phi kham thay doi theo dich vu, chuyen khoa va chi dinh cua bac si. Vui long lien he hotline de duoc bao gia cap nhat.";
        }
        String priceLines = prices.stream()
                .limit(5)
                .map(price -> {
                    String range = price.getMaxPrice() != null
                            ? String.format("%,.0f - %,.0f VND", price.getMinPrice(), price.getMaxPrice())
                            : String.format("%,.0f VND", price.getMinPrice());
                    return price.getServiceName() + ": " + range;
                })
                .reduce((left, right) -> left + "; " + right)
                .orElse("");
        return "Bang gia tham khao: " + priceLines + ". Chi phi thuc te phu thuoc chi dinh bac si, thuoc, vat tu va can lam sang phat sinh.";
    }

    private void appendDepartmentInfo(StringBuilder target, String department) {
        String normalizedDepartment = normalize(department);
        Optional<DepartmentInfo> info = departmentInfoRepository.findAll().stream()
                .filter(item -> normalizedDepartment.contains(normalize(item.getName()))
                        || normalize(item.getName()).contains(normalizedDepartment))
                .findFirst();
        info.ifPresent(item -> target.append(" V\u1ecb tr\u00ed g\u1ee3i \u00fd: ")
                .append(repairReplacementArtifacts(repairUtf8Deep(item.getLocation())))
                .append(", gi\u1edd ti\u1ebfp nh\u1eadn: ")
                .append(repairReplacementArtifacts(repairUtf8Deep(item.getWorkingHours())))
                .append("."));
    }

    private AssistantResponse withDisclaimer(AssistantResponse response) {
        String advice = response.getAdvice();
        if (advice == null || !normalize(advice).contains("khong thay the")) {
            advice = (advice == null || advice.isBlank() ? "" : advice + " ") + MEDICAL_DISCLAIMER;
        }
        return sanitizeResponse(new AssistantResponse(
                response.getType(),
                response.getDepartment(),
                response.getRiskScore(),
                response.getRiskLevel(),
                response.getAnswer(),
                advice,
                response.isEmergency()
        ));
    }

    private AssistantResponse enrichWithGemini(String rawMessage, AssistantResponse response) {
        if (!"TRIAGE".equals(response.getType()) && !"EMERGENCY".equals(response.getType())) {
            return response;
        }
        String summary = response.getAnswer() + " " + response.getAdvice();
        Optional<String> aiNote = openAiAssistantService.generateTriageNote(rawMessage, summary);
        if (aiNote.isEmpty()) {
            aiNote = geminiAssistantService.generateTriageNote(rawMessage, summary);
        }
        return aiNote
                .map(note -> new AssistantResponse(
                        response.getType(),
                        response.getDepartment(),
                        response.getRiskScore(),
                        response.getRiskLevel(),
                        response.getAnswer(),
                        response.getAdvice() + " Ghi chÃº AI: " + repairUtf8(note),
                        response.isEmergency()))
                .orElse(response);
    }

    private void persistConversation(String message, boolean authenticated, String source, AssistantResponse response) {
        try {
            ChatbotConversationLog log = new ChatbotConversationLog();
            log.setUserMessage(message == null || message.isBlank() ? "(empty)" : message.trim());
            log.setResponseType(response.getType());
            log.setDepartment(response.getDepartment());
            log.setRiskScore(response.getRiskScore());
            log.setRiskLevel(response.getRiskLevel());
            log.setEmergency(response.isEmergency());
            log.setAuthenticated(authenticated);
            log.setSource(source);
            chatbotConversationLogRepository.save(log);
        } catch (RuntimeException ignored) {
            // Chatbot must still answer if logging is temporarily unavailable.
        }
    }

    private String suggestDepartment(MedicalIntent intent) {
        if (intent.chestPain || intent.palpitations || intent.bloodPressureIssue || intent.vagueCardiacConcern) return "Tim máº¡ch";
        if (intent.weaknessOrNumbness || intent.slurredSpeech || intent.facialDroop || intent.headache) return "Tháº§n kinh";
        if (intent.shortBreath || intent.cough) return "HÃ´ háº¥p";
        if (intent.soreThroat || intent.runnyNose || intent.earPain) return "Tai mÅ©i há»ng";
        if (intent.limbPain) return "CÆ¡ xÆ°Æ¡ng khá»›p hoáº·c Tháº§n kinh";
        if (intent.jointPain) return "CÆ¡ xÆ°Æ¡ng khá»›p";
        if (intent.stomachPain || intent.diarrhea || intent.vomiting || intent.dehydration) return "TiÃªu hÃ³a";
        if (intent.urinaryIssue) return "Ná»™i tá»•ng quÃ¡t";
        if (intent.diabetesIssue) return "Ná»™i tá»•ng quÃ¡t";
        if (intent.rash || intent.skinDisease) return "Da liá»…u";
        if (intent.child) return "Nhi khoa";
        if (intent.obgyn) return "Sáº£n phá»¥ khoa";
        return "Ná»™i tá»•ng quÃ¡t";
    }

    private int calculateRiskScore(MedicalIntent intent) {
        int score = 0;
        if (intent.shortBreath) score += 4;
        if (intent.chestPain) score += 3;
        if (intent.palpitations) score += 2;
        if (intent.highFever) score += 2;
        if (intent.seizure) score += 5;
        if (intent.unconscious) score += 5;
        if (intent.heavyBleeding) score += 5;
        if (intent.bloodPressureIssue) score += 2;
        if (intent.diarrhea || intent.vomiting) score += 2;
        if (intent.dehydration) score += 3;
        if (intent.urinaryIssue) score += 2;
        if (intent.diabetesIssue) score += 2;
        if (intent.weaknessOrNumbness || intent.slurredSpeech || intent.facialDroop) score += 4;
        if (intent.infectiousDisease && intent.highFever) score += 2;
        if (intent.bleedingSpots) score += 3;
        if (intent.child || intent.obgyn) score += 1;
        if (score == 0 && intent.hasAnySymptom()) score = 2;
        return Math.min(score, 10);
    }

    private boolean isEmergencyIntent(MedicalIntent intent) {
        return intent.seizure
                || intent.unconscious
                || intent.heavyBleeding
                || (intent.chestPain && intent.shortBreath)
                || (intent.bloodPressureIssue && (intent.headache || intent.chestPain || intent.shortBreath))
                || intent.strokeLikeSymptoms()
                || (intent.infectiousDisease && intent.highFever && intent.bleedingSpots)
                || ((intent.diarrhea || intent.vomiting) && intent.dehydration)
                || (intent.diabetesIssue && intent.dehydration);
    }

    private boolean hasRedFlag(MedicalIntent intent) {
        return isEmergencyIntent(intent)
                || (intent.covidLike && intent.shortBreath)
                || (intent.infectiousDisease && intent.highFever)
                || (intent.palpitations && intent.shortBreath);
    }

    private String resolveRiskLevel(int riskScore) {
        if (riskScore >= 7) return "nguy hiá»ƒm";
        if (riskScore >= 4) return "trung bÃ¬nh";
        return "nháº¹";
    }

    private MedicalIntent detectMedicalIntent(String normalized) {
        MedicalIntent intent = new MedicalIntent();
        intent.chestPain = containsAny(normalized, "dau nguc", "tuc nguc");
        intent.vagueCardiacConcern = containsAny(normalized, "dau tim", "benh tim", "tim bi dau");
        intent.palpitations = containsAny(normalized, "hoi hop", "tim dap nhanh", "danh trong nguc", "nhip tim nhanh", "roi loan nhip");
        intent.shortBreath = containsAny(normalized, "kho tho", "tho gap", "hut hoi", "tho khong duoc");
        intent.stomachPain = containsAny(normalized, "dau bung", "dau da day", "viem da day", "quan bung", "quan dau bung", "trao nguoc", "day bung");
        intent.headache = containsAny(normalized, "dau dau", "nhuc dau", "chong mat", "choang vang", "dau nua dau");
        intent.weaknessOrNumbness = containsAny(normalized, "te tay chan", "yeu tay chan", "te nua nguoi", "mat suc tay chan", "tay chan khong co luc");
        intent.slurredSpeech = containsAny(normalized, "noi kho", "noi lap bap", "khong noi ro", "luoi noi");
        intent.facialDroop = containsAny(normalized, "meo mieng", "liet mat", "xech mieng");
        intent.limbPain = containsAny(normalized, "dau tay", "dau chan", "nhuc tay", "nhuc chan", "moi tay", "moi chan");
        intent.jointPain = containsAny(normalized, "dau xuong khop", "dau khop", "nhuc khop", "nhuc xuong", "sung khop");
        intent.cough = containsAny(normalized, "ho keo dai", "ho lau ngay", "ho nhieu ngay", "ho lien tuc", "ho khan", "ho co dom", "kho khe");
        intent.rash = containsAny(normalized, "noi man", "phat ban", "di ung da", "ngua nhieu", "noi me day");
        intent.highFever = containsAny(normalized, "sot cao", "nong sot cao", "sot tren 39");
        intent.seizure = containsAny(normalized, "co giat");
        intent.unconscious = containsAny(normalized, "mat y thuc", "bat tinh", "ngat xiu");
        intent.heavyBleeding = containsAny(normalized, "chay mau nhieu", "xuat huyet nhieu", "mau chay nhieu");
        intent.bleedingSpots = containsAny(normalized, "xuat huyet duoi da", "cham do duoi da", "bong huyet", "chay mau chan rang", "chay mau cam");
        intent.dehydration = containsAny(normalized, "kho moi", "khat nuoc nhieu", "mat nuoc", "met lu", "nguoi lang", "tieu it");
        intent.child = containsAny(normalized, "tre em", "em be", "be bi", "con toi");
        intent.obgyn = containsAny(normalized, "mang thai", "san khoa", "phu khoa", "kinh nguyet", "thai ky");
        intent.soreThroat = containsAny(normalized, "dau hong", "rat hong", "viem hong", "viem amidan", "khan tieng");
        intent.runnyNose = containsAny(normalized, "so mui", "nghet mui", "chay nuoc mui", "viem mui", "hat hoi");
        intent.diarrhea = containsAny(normalized, "tieu chay", "di ngoai nhieu", "phan long", "roi loan tieu hoa");
        intent.vomiting = containsAny(normalized, "non", "oi", "buon non", "non nhieu");
        intent.toothPain = containsAny(normalized, "dau rang", "sung loi", "viem loi", "sau rang");
        intent.earPain = containsAny(normalized, "dau tai", "u tai", "chay dich tai", "viem tai");
        intent.eyePain = containsAny(normalized, "do mat", "dau mat", "com mat", "mo mat", "ngua mat", "viem ket mac");
        intent.urinaryIssue = containsAny(normalized, "tieu buot", "tieu rat", "tieu nhieu", "dau khi tieu", "viem duong tiet nieu", "tieu ra mau");
        intent.bloodPressureIssue = containsAny(normalized, "tang huyet ap", "ha huyet ap", "huyet ap cao", "huyet ap thap");
        intent.diabetesIssue = containsAny(normalized, "tieu duong", "duong huyet cao", "tieu nhieu bat thuong", "sut can", "ha duong huyet");
        intent.covidLike = containsAny(normalized, "covid", "covid 19", "corona");
        intent.infectiousDisease = containsAny(normalized, "cum", "covid", "sot xuat huyet", "thuy dau", "soi", "tay chan mieng");
        intent.skinDisease = containsAny(normalized, "viem da", "nam da", "eczema", "a sung", "mun mu", "mun nhot");
        return intent;
    }

    private boolean isInventoryIntent(String normalized) {
        return containsAny(normalized,
                "vat tu", "ton kho", "kho", "het han", "han dung", "sap het", "duoi muc ton",
                "thuoc", "ma vat tu", "tra cuu", "tim ma", "qr", "bao cao nhanh");
    }

    private Optional<String> extractSupplyCode(String rawMessage) {
        Matcher matcher = SUPPLY_CODE_PATTERN.matcher(rawMessage);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private String extractSearchKeyword(String normalized) {
        String keyword = normalized
                .replace("tra cuu", "")
                .replace("tim vat tu", "")
                .replace("ma vat tu", "")
                .replace("vat tu", "")
                .replace("thuoc", "")
                .replace("qr", "")
                .replace("co bao nhieu", "")
                .replace("con bao nhieu", "")
                .trim();
        return keyword.replaceAll("\\s+", " ");
    }

    private String formatStatus(MedicalSupply supply) {
        if (supply.getQuantity() == null || supply.getQuantity() <= 0) return "háº¿t hÃ ng";
        if (supply.getQuantity() <= supply.getMinimumStock()) return "dÆ°á»›i má»©c tá»“n";
        if (supply.getExpiryDate() != null && !supply.getExpiryDate().isAfter(LocalDate.now().plusDays(30))) return "sáº¯p háº¿t háº¡n";
        return "á»•n Ä‘á»‹nh";
    }

    private boolean containsNormalized(String value, String keyword) {
        return normalize(value).contains(keyword);
    }

    private boolean containsAny(String source, String... phrases) {
        for (String phrase : phrases) {
            if (source.contains(phrase)) {
                return true;
            }
        }
        return false;
    }

        private String normalize(String input) {
        String normalized = Normalizer.normalize(repairUtf8(input), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0111', 'd')
                .replace('\u0110', 'd')
                .toLowerCase(Locale.ROOT);

        StringBuilder clean = new StringBuilder();
        for (char ch : normalized.toCharArray()) {
            if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)) {
                clean.append(ch);
            } else {
                clean.append(' ');
            }
        }

        return clean.toString().replaceAll("\\s+", " ").trim();
    }

    private AssistantResponse sanitizeResponse(AssistantResponse response) {
        return new AssistantResponse(
                repairUtf8Deep(response.getType()),
                repairUtf8Deep(response.getDepartment()),
                response.getRiskScore(),
                repairUtf8Deep(response.getRiskLevel()),
                repairUtf8Deep(response.getAnswer()),
                repairUtf8Deep(response.getAdvice()),
                response.isEmergency()
        );
    }

    private String repairUtf8Deep(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String repaired = value;
        for (int i = 0; i < 3 && looksMojibake(repaired); i++) {
            try {
                repaired = new String(repaired.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
            } catch (RuntimeException ex) {
                break;
            }
        }
        return repairReplacementArtifacts(repaired);
    }

    private boolean looksMojibake(String value) {
        return value.contains("Ã")
                || value.contains("Â")
                || value.contains("Ä")
                || value.contains("áº")
                || value.contains("á»")
                || value.contains("Æ")
                || value.contains("\uFFFD");
    }

    private String repairReplacementArtifacts(String value) {
        return value
                .replace("nghi ng\uFFFD?", "nghi ng\u1edd")
                .replace("s\u00e0ng l\uFFFD?c", "s\u00e0ng l\u1ecdc")
                .replace("T?ng", "T\u1ea7ng")
                .replace("t? th? 2 ??n th? 7", "t\u1eeb th\u1ee9 2 \u0111\u1ebfn th\u1ee9 7")
                .replace("gio tiep nhan", "gi\u1edd ti\u1ebfp nh\u1eadn")
                .replace("Vi tri goi y", "V\u1ecb tr\u00ed g\u1ee3i \u00fd")
                .replace("Luu y: thong tin nay chi ho tro sang loc so bo, khong thay the chan doan hoac chi dinh dieu tri cua bac si.",
                        "L\u01b0u \u00fd: th\u00f4ng tin n\u00e0y ch\u1ec9 h\u1ed7 tr\u1ee3 s\u00e0ng l\u1ecdc s\u01a1 b\u1ed9, kh\u00f4ng thay th\u1ebf ch\u1ea9n \u0111o\u00e1n ho\u1eb7c ch\u1ec9 \u0111\u1ecbnh \u0111i\u1ec1u tr\u1ecb c\u1ee7a b\u00e1c s\u0129.")
                .replace("nh\uFFFDm", "nh\u00f3m")
                .replace("b\uFFFD?nh", "b\u1ec7nh")
                .replace("hi\uFFFD?n", "hi\u1ec7n")
                .replace("hi\uFFFD?u", "hi\u1ec7u")
                .replace("c\uFFFD?p", "c\u1ea5p")
                .replace("c\uFFFD?u", "c\u1ee9u")
                .replace("d\uFFFD?u", "d\u1ea5u")
                .replace("n\uFFFDi", "n\u00f3i")
                .replace("kh\uFFFD", "kh\u00f3")
                .replace("thay th\uFFFD", "thay th\u1ebf")
                .replace("ch\uFFFD?n", "ch\u1ea9n")
                .replace("\u0111i\uFFFD?u", "\u0111i\u1ec1u");
    }
    private String repairUtf8(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (!(value.contains("Ã") || value.contains("Ä") || value.contains("áº") || value.contains("á»"))) {
            return value;
        }
        try {
            return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        } catch (RuntimeException ex) {
            return value;
        }
    }

    private static final class MedicalIntent {
        private boolean chestPain;
        private boolean vagueCardiacConcern;
        private boolean palpitations;
        private boolean shortBreath;
        private boolean stomachPain;
        private boolean headache;
        private boolean weaknessOrNumbness;
        private boolean slurredSpeech;
        private boolean facialDroop;
        private boolean limbPain;
        private boolean jointPain;
        private boolean cough;
        private boolean rash;
        private boolean highFever;
        private boolean seizure;
        private boolean unconscious;
        private boolean heavyBleeding;
        private boolean bleedingSpots;
        private boolean dehydration;
        private boolean child;
        private boolean obgyn;
        private boolean soreThroat;
        private boolean runnyNose;
        private boolean diarrhea;
        private boolean vomiting;
        private boolean toothPain;
        private boolean earPain;
        private boolean eyePain;
        private boolean urinaryIssue;
        private boolean bloodPressureIssue;
        private boolean diabetesIssue;
        private boolean covidLike;
        private boolean infectiousDisease;
        private boolean skinDisease;

        private boolean hasAnySymptom() {
            return chestPain || vagueCardiacConcern || palpitations || shortBreath || stomachPain || headache
                    || weaknessOrNumbness || slurredSpeech || facialDroop || limbPain || jointPain || cough
                    || rash || highFever || seizure || unconscious || heavyBleeding || bleedingSpots || dehydration
                    || child || obgyn || soreThroat || runnyNose || diarrhea || vomiting || toothPain || earPain
                    || eyePain || urinaryIssue || bloodPressureIssue || diabetesIssue || covidLike
                    || infectiousDisease || skinDisease;
        }

        private boolean strokeLikeSymptoms() {
            return facialDroop || slurredSpeech || weaknessOrNumbness;
        }
    }
}

