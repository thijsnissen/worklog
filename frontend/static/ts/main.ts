import {
    deleteButton,
    exportByIdsButton,
    importInRangeButton,
    initialize,
    printButton,
    renderTable,
    toggleAllCheckboxes,
    updateButtons,
    updateSortKey,
} from "./service/service.js";
import {
    DELETE_BUTTON_ID,
    EXPORT_BY_IDS_BUTTON_ID,
    IMPORT_IN_RANGE_BUTTON_ID,
    PRINT_BUTTON_ID,
    WORKLOGS_SORTABLE_KEYS,
} from "./types/constants.js";
import { Worklog } from "./types/aliasses";

document.addEventListener("DOMContentLoaded", (): void => {
    document.addEventListener("click", (e: MouseEvent) => {
        if (e.target instanceof HTMLElement) {
            void handleClick(e.target);
        }
    });

    document.addEventListener("change", (e: Event) => {
        if (e.target instanceof HTMLInputElement) {
            handleChange(e.target);
        }
    });

    void initialize();
});

const handleClick = async (target: HTMLElement) => {
    switch (target.id) {
        case IMPORT_IN_RANGE_BUTTON_ID:
            await importInRangeButton();
            return;
        case EXPORT_BY_IDS_BUTTON_ID:
            await exportByIdsButton();
            return;
        case DELETE_BUTTON_ID:
            await deleteButton();
            return;
        case PRINT_BUTTON_ID:
            printButton();
            return;
    }

    if (target.matches(".sort-container")) {
        const key = target?.closest("th[id]")?.id;

        if (key && WORKLOGS_SORTABLE_KEYS.includes(key as keyof Worklog)) {
            updateSortKey(key as keyof Worklog);
            renderTable();
        }
    }
};

const handleChange = (target: HTMLInputElement) => {
    switch (target.id) {
        case "toggleAll":
            toggleAllCheckboxes(target.checked);
            return;
    }

    if (
        target.matches(".checkbox") ||
        target.matches("#startDateTimeInclusive") ||
        target.matches("#endDateTimeInclusive")
    ) {
        updateButtons();
    }
};
