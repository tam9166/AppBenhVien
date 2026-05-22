function addReceiptRow(type) {
    const container = document.querySelector(`[data-collection="${type}"]`);
    if (!container) return;

    const currentRows = container.querySelectorAll(".detail-row");
    const index = currentRows.length;
    const firstRow = currentRows[0];
    if (!firstRow) return;

    const clone = firstRow.cloneNode(true);
    clone.querySelectorAll("select, input").forEach((element) => {
        const name = element.getAttribute("name");
        const id = element.getAttribute("id");
        if (name) element.setAttribute("name", name.replace(/\[\d+]/, `[${index}]`));
        if (id) element.setAttribute("id", id.replace(/_\d+__/, `_${index}__`));
        element.value = "";
    });
    container.appendChild(clone);
}

document.addEventListener("DOMContentLoaded", () => {
    initCharts();
    initAssistant();
    initAppointmentSuggestion();
});

function initCharts() {
    const doughnutLabelPlugin = {
        id: "doughnutLabelPlugin",
        afterDatasetsDraw(chart) {
            if (chart.config.type !== "doughnut") return;
            const dataset = chart.data.datasets[0];
            const total = dataset.data.reduce((sum, value) => sum + value, 0);
            if (!total) return;

            const {ctx} = chart;
            ctx.save();
            ctx.font = "600 12px Segoe UI";
            ctx.fillStyle = "#12324a";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";

            chart.getDatasetMeta(0).data.forEach((arc, index) => {
                const value = Number(dataset.data[index] || 0);
                if (!value) return;
                const percentage = Math.round((value / total) * 100);
                const position = arc.tooltipPosition();
                ctx.fillText(`${percentage}%`, position.x, position.y);
            });

            ctx.restore();
        }
    };

    const chartCanvas = document.getElementById("dashboardChart");
    if (chartCanvas && typeof Chart !== "undefined") {
        new Chart(chartCanvas, {
            type: "line",
            data: {
                labels: JSON.parse(chartCanvas.dataset.labels),
                datasets: [
                    {label: "Nhập kho", data: JSON.parse(chartCanvas.dataset.inbound), borderColor: "#1677ff", backgroundColor: "rgba(22,119,255,0.12)", tension: 0.3, fill: true},
                    {label: "Xuất kho", data: JSON.parse(chartCanvas.dataset.outbound), borderColor: "#0ea5a4", backgroundColor: "rgba(14,165,164,0.12)", tension: 0.3, fill: true}
                ]
            },
            options: {responsive: true, plugins: {legend: {position: "top"}}}
        });
    }

    const categoryCanvas = document.getElementById("categoryChart");
    if (categoryCanvas && typeof Chart !== "undefined") {
        new Chart(categoryCanvas, {
            type: "doughnut",
            data: {
                labels: JSON.parse(categoryCanvas.dataset.labels),
                datasets: [{data: JSON.parse(categoryCanvas.dataset.values), backgroundColor: ["#1677ff", "#0ea5a4", "#ffb020", "#ef4444", "#7c3aed"]}]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {position: "bottom"},
                    tooltip: {
                        callbacks: {
                            label(context) {
                                const values = context.dataset.data || [];
                                const total = values.reduce((sum, value) => sum + value, 0);
                                const value = Number(context.raw || 0);
                                const percentage = total ? ((value / total) * 100).toFixed(1) : 0;
                                return `${context.label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                }
            },
            plugins: [doughnutLabelPlugin]
        });
    }
}

function initAssistant() {
    const form = document.getElementById("assistantForm");
    if (!form) return;

    const messageInput = document.getElementById("assistantMessage");
    const summaryBlock = document.getElementById("assistantSummary");
    const badge = document.getElementById("assistantBadge");
    const submitLabel = form.querySelector(".assistant-submit-label");
    const suggestionButtons = document.querySelectorAll(".assistant-suggestion");
    const history = [];

    suggestionButtons.forEach((button) => {
        button.addEventListener("click", () => {
            messageInput.value = button.dataset.message || "";
            messageInput.focus();
        });
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const message = messageInput.value.trim();
        if (!message) {
            updateAssistantState(badge, "Cần nội dung", "text-bg-warning");
            summaryBlock.innerHTML = '<div class="empty-state mb-0">Vui lòng nhập câu hỏi hoặc mô tả triệu chứng.</div>';
            return;
        }

        const requestMessage = history.length
                ? `${history.slice(-2).map((item) => `Người bệnh: ${item.user}\nTrợ lý: ${item.answer}`).join("\n")}\nNgười bệnh: ${message}`
                : message;

        form.classList.add("is-loading");
        if (submitLabel) submitLabel.textContent = "Đang phân tích...";
        updateAssistantState(badge, "Đang xử lý", "text-bg-info");
        summaryBlock.innerHTML = '<div class="empty-state mb-0">Trợ lý đang phân tích nội dung bạn vừa gửi.</div>';

        try {
            const response = await fetch(`/api/assistant?message=${encodeURIComponent(requestMessage)}`, {headers: {Accept: "application/json"}});
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            history.push({user: message, answer: data.answer});
            renderAssistantSummary(summaryBlock, data, message);
            updateAssistantState(badge, data.emergency ? "Cần ưu tiên" : "Hoàn tất", data.emergency ? "text-bg-danger" : "text-bg-success");
        } catch (error) {
            updateAssistantState(badge, "Lỗi kết nối", "text-bg-danger");
            summaryBlock.innerHTML = '<div class="empty-state mb-0">Không thể lấy kết quả từ trợ lý. Vui lòng thử lại.</div>';
            console.error(error);
        } finally {
            form.classList.remove("is-loading");
            if (submitLabel) submitLabel.textContent = "Gửi yêu cầu";
        }
    });
}

function renderAssistantSummary(target, data, originalMessage = "") {
    const typeLabel = data.type === "EMERGENCY" ? "Cấp cứu"
        : data.type === "TRIAGE" ? "Sàng lọc"
        : data.type === "INVENTORY" ? "Kho vật tư"
        : "Thông tin";
    const department = data.department || "Chưa xác định";
    const emergencyClass = data.emergency ? "assistant-risk emergency" : "assistant-risk";
    const emergencyActions = data.emergency ? `
        <div class="assistant-emergency-actions mt-3">
            <strong>Cần ưu tiên xử trí</strong>
            <p class="mb-2">Nếu triệu chứng đang diễn tiến nặng, hãy đến khoa Cấp cứu hoặc gọi hotline bệnh viện.</p>
            <a class="btn btn-danger btn-sm" href="tel:19001234">Gọi hotline</a>
            <a class="btn btn-outline-danger btn-sm" href="/patient/appointments/new">Đặt lịch/tiếp nhận</a>
        </div>` : "";
    const followUpActions = data.type === "TRIAGE" || data.type === "EMERGENCY" ? `
        <form class="mt-3 d-flex gap-2 flex-wrap" method="post" action="/patient/screening-ticket">
            <input type="hidden" name="message" value="${escapeHtml(originalMessage)}">
            <button class="btn btn-outline-primary btn-sm" type="submit">Tạo phiếu sàng lọc</button>
            <button class="btn btn-outline-secondary btn-sm assistant-follow-up" type="button" data-message="${escapeHtml(originalMessage + " Triệu chứng bắt đầu từ khi nào? Có nặng lên nhanh không?")}">Hỏi tiếp thời gian</button>
            <button class="btn btn-outline-secondary btn-sm assistant-follow-up" type="button" data-message="${escapeHtml(originalMessage + " Có sốt, khó thở, đau dữ dội hoặc yếu tay chân không?")}">Hỏi tiếp dấu hiệu nguy hiểm</button>
        </form>` : "";

    target.innerHTML = `
        <div class="assistant-summary-grid">
            <div class="assistant-risk-card"><span class="assistant-label">Loại phản hồi</span><strong>${escapeHtml(typeLabel)}</strong></div>
            <div class="assistant-risk-card"><span class="assistant-label">Khoa gợi ý</span><strong>${escapeHtml(department)}</strong></div>
            <div class="${emergencyClass}">
                <span class="assistant-label">Mức độ</span>
                <strong>${escapeHtml(data.risk_level || "nhẹ")}</strong>
                <small>Điểm nguy cơ: ${escapeHtml(String(data.risk_score ?? 0))}</small>
            </div>
        </div>
        <div class="assistant-copy mt-3">
            <p class="mb-2"><strong>Trả lời:</strong> ${escapeHtml(data.answer || "")}</p>
            <p class="mb-0"><strong>Hướng dẫn:</strong> ${escapeHtml(data.advice || "")}</p>
        </div>
        ${emergencyActions}
        ${followUpActions}
    `;

    target.querySelectorAll(".assistant-follow-up").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById("assistantMessage");
            if (input) {
                input.value = button.dataset.message || "";
                input.focus();
            }
        });
    });
}

function initAppointmentSuggestion() {
    const button = document.getElementById("appointmentSuggestButton");
    if (!button) return;

    const symptomsInput = document.getElementById("appointmentSymptoms");
    const departmentSelect = document.querySelector('select[name="department"]');
    const priorityInput = document.querySelector('input[name="priorityLevel"]');
    const emergencyInput = document.querySelector('input[name="emergency"]');
    const triageSummaryInput = document.querySelector('input[name="triageSummary"]');
    const result = document.getElementById("appointmentSuggestionResult");

    button.addEventListener("click", async () => {
        const message = symptomsInput.value.trim();
        if (!message) {
            result.textContent = "Vui lòng nhập triệu chứng trước khi phân tích.";
            return;
        }
        button.disabled = true;
        result.textContent = "Đang phân tích triệu chứng...";
        try {
            const response = await fetch(`/api/assistant?message=${encodeURIComponent(message)}`, {headers: {Accept: "application/json"}});
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();
            if ((!departmentSelect.value || departmentSelect.value === "") && data.department) {
                departmentSelect.value = data.department;
            }
            if (priorityInput) priorityInput.value = data.risk_level || "";
            if (emergencyInput) emergencyInput.value = Boolean(data.emergency);
            if (triageSummaryInput) triageSummaryInput.value = `${data.answer || ""}\n${data.advice || ""}`.trim();
            result.innerHTML = `<strong>Khoa gợi ý:</strong> ${escapeHtml(data.department || "Chưa xác định")}<br><strong>Mức ưu tiên:</strong> ${escapeHtml(data.risk_level || "nhẹ")}<br><strong>Tóm tắt:</strong> ${escapeHtml(data.answer || "")}`;
        } catch (error) {
            result.textContent = "Không thể phân tích triệu chứng lúc này.";
            console.error(error);
        } finally {
            button.disabled = false;
        }
    });
}

function updateAssistantState(badge, text, badgeClass) {
    if (!badge) return;
    badge.className = `badge ${badgeClass}`;
    badge.textContent = text;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
