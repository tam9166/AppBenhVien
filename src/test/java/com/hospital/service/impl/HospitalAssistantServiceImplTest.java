package com.hospital.service.impl;

import com.hospital.dto.AssistantResponse;
import com.hospital.entity.MedicalServicePrice;
import com.hospital.repository.ChatbotConversationLogRepository;
import com.hospital.repository.DepartmentInfoRepository;
import com.hospital.repository.MedicalServicePriceRepository;
import com.hospital.repository.MedicalSupplyRepository;
import com.hospital.service.GeminiAssistantService;
import com.hospital.service.OpenAiAssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HospitalAssistantServiceImplTest {

    private MedicalSupplyRepository medicalSupplyRepository;
    private DepartmentInfoRepository departmentInfoRepository;
    private MedicalServicePriceRepository medicalServicePriceRepository;
    private ChatbotConversationLogRepository chatbotConversationLogRepository;
    private GeminiAssistantService geminiAssistantService;
    private OpenAiAssistantService openAiAssistantService;

    private HospitalAssistantServiceImpl service;

    @BeforeEach
    public void setUp() {
        medicalSupplyRepository = mock(MedicalSupplyRepository.class);
        departmentInfoRepository = mock(DepartmentInfoRepository.class);
        medicalServicePriceRepository = mock(MedicalServicePriceRepository.class);
        chatbotConversationLogRepository = mock(ChatbotConversationLogRepository.class);
        geminiAssistantService = mock(GeminiAssistantService.class);
        openAiAssistantService = mock(OpenAiAssistantService.class);

        when(departmentInfoRepository.findAll()).thenReturn(List.of());
        when(medicalServicePriceRepository.findAll()).thenReturn(List.of());
        when(openAiAssistantService.generateTriageNote(any(), any())).thenReturn(Optional.empty());
        when(geminiAssistantService.generateTriageNote(any(), any())).thenReturn(Optional.empty());

        service = new HospitalAssistantServiceImpl(
                medicalSupplyRepository,
                departmentInfoRepository,
                medicalServicePriceRepository,
                chatbotConversationLogRepository,
                geminiAssistantService,
                openAiAssistantService);
    }

    @Test
    public void shouldMarkFacialDroopAsEmergency() {
        AssistantResponse response = service.analyze("Toi bi meo mieng va noi kho", false);

        assertEquals("EMERGENCY", response.getType());
        assertTrue(response.isEmergency());
        assertEquals("Thần kinh", response.getDepartment());
    }

    @Test
    public void shouldMarkDengueWarningAsEmergency() {
        AssistantResponse response = service.analyze("Toi sot xuat huyet, sot cao va co cham do duoi da", false);

        assertEquals("EMERGENCY", response.getType());
        assertTrue(response.isEmergency());
        assertTrue(response.getAnswer().contains("cảnh báo đỏ") || response.getAnswer().contains("Cảnh báo đỏ"));
    }

    @Test
    public void shouldRouteHypertensionWithChestPainToCardiologyEmergency() {
        AssistantResponse response = service.analyze("Toi tang huyet ap va dau nguc", false);

        assertEquals("EMERGENCY", response.getType());
        assertTrue(response.isEmergency());
        assertEquals("Tim mạch", response.getDepartment());
    }

    @Test
    public void shouldRoutePalpitationsToCardiology() {
        AssistantResponse response = service.analyze("Toi bi hoi hop tim dap nhanh", false);

        assertEquals("TRIAGE", response.getType());
        assertEquals("Tim mạch", response.getDepartment());
    }

    @Test
    public void shouldAnswerServicePriceQuestion() {
        MedicalServicePrice price = new MedicalServicePrice();
        price.setServiceName("Khám thường");
        price.setMinPrice(BigDecimal.valueOf(100000));
        price.setMaxPrice(BigDecimal.valueOf(150000));
        when(medicalServicePriceRepository.findAll()).thenReturn(List.of(price));

        AssistantResponse response = service.analyze("Chi phi kham thuong bao nhieu tien?", false);

        assertEquals("FAQ", response.getType());
        assertTrue(response.getAnswer().contains("Khám thường"));
        assertTrue(response.getAnswer().contains("100,000"));
    }

    @Test
    public void shouldAppendMedicalDisclaimerForTriage() {
        AssistantResponse response = service.analyze("Toi bi ho keo dai va sot cao", false);

        assertTrue(response.getAdvice().contains("khong thay the"));
    }

    @Test
    public void shouldPersistConversationLog() {
        service.analyze("Toi bi dau bung va non", false);

        verify(chatbotConversationLogRepository).save(any());
    }
}
