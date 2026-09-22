"use strict";

const steps = [
    {
        layer: "INBOUND ADAPTER",
        title: "HTTP を、ユースケースの入力へ。",
        copy: "Controller が @Valid で必須項目を検証し、CreateReservationRequest を CreateReservationCommand に変換。入力ポートを通じてサービスを呼び出します。",
        code: "CreateReservationRequest\n    → CreateReservationCommand\n    → CreateReservationUseCase.create()"
    },
    {
        layer: "APPLICATION + DOMAIN",
        title: "正しい期間か、空いているか。",
        copy: "ReservationPeriod が開始・終了時刻を検証。サービスは Repository Port を通じて、同じ会議室の CONFIRMED 予約との重複を確認します。重複があれば DuplicateReservationException を送出します。",
        code: "new ReservationPeriod(startAt, endAt)\nrepositoryPort.existsConfirmedReservationOverlapping(...)\n    → 重複あり: HTTP 409"
    },
    {
        layer: "DOMAIN MODEL",
        title: "新しい予約を、CONFIRMED で生成。",
        copy: "サービスが UUID を発行し、Reservation.create() で予約を生成。ドメインモデルは会議室・予約者・期間を持ち、初期状態を CONFIRMED に設定します。",
        code: "Reservation.create(\n    UUID.randomUUID(), resourceId, memberId, period\n) // status = CONFIRMED"
    },
    {
        layer: "OUTBOUND ADAPTER + DATABASE",
        title: "保存の瞬間にも、競合を防ぐ。",
        copy: "JPA アダプターのトランザクション内で会議室単位のロックを取得し、saveAndFlush() で保存。DB の排他制約に違反した場合、SQLSTATE 23P01 を DuplicateReservationException に変換します。",
        code: "pg_advisory_xact_lock(resourceLockKey)\nsaveAndFlush(entity)\nEXCLUDE USING gist (... tsrange(start_at, end_at, '[)') WITH &&)"
    },
    {
        layer: "HTTP RESPONSE",
        title: "結果は、来た道を戻って UI へ。",
        copy: "保存結果を JPA アダプターからサービス、Controller へ返します。Controller が ReservationResponse に変換し、201 Created と予約 ID・期間・状態を UI に返します。競合時は例外ハンドラーが 409 に変換します。",
        code: "Repository → Service → Controller → UI\nHTTP 201 Created\n{ id, resourceId, memberId, startAt, endAt, status }"
    }
];

const tabs = [...document.querySelectorAll("[data-step]")];
let activeStep = 0;

function selectStep(index, focus = false) {
    activeStep = index;
    const step = steps[index];
    tabs.forEach((tab, i) => {
        tab.setAttribute("aria-selected", String(i === index));
        tab.tabIndex = i === index ? 0 : -1;
    });
    document.getElementById("step-detail").setAttribute("aria-labelledby", "step-" + index);
    document.getElementById("step-layer").textContent = step.layer;
    document.getElementById("step-title").textContent = step.title;
    document.getElementById("step-copy").textContent = step.copy;
    document.getElementById("step-code").textContent = step.code;
    document.getElementById("step-count").textContent = "0" + (index + 1) + " / 05";
    document.getElementById("next-step").textContent = index === steps.length - 1 ? "最初に戻る ↺" : "次のステップ →";
    if (focus) tabs[index].focus();
}

tabs.forEach((tab, index) => {
    tab.addEventListener("click", () => selectStep(index));
    tab.addEventListener("keydown", event => {
        let next;
        if (event.key === "ArrowDown") next = (index + 1) % steps.length;
        if (event.key === "ArrowUp") next = (index + steps.length - 1) % steps.length;
        if (event.key === "Home") next = 0;
        if (event.key === "End") next = steps.length - 1;
        if (next !== undefined) {
            event.preventDefault();
            selectStep(next, true);
        }
    });
});

document.getElementById("next-step").addEventListener("click", () => {
    selectStep((activeStep + 1) % steps.length);
});
