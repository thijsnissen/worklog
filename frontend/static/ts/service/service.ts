import {
    formatDuration,
    generateIcons,
    getCheckedIds,
    safeQueryParent,
    setLoading,
    showError,
    showSuccess,
    validateDateTimeRange,
    validateNonEmptyList,
} from "../utils.js";
import { stateHandler } from "./stateHandler";
import { Worklog } from "../types/aliasses.js";
import { deleteByIds, exportByIds, flush, getAll, importInRange } from "../api/api.js";
import {
    DEFAULT_WORKLOGS_TABLE,
    DELETE_BUTTON_ID,
    END_DATE_TIME_INCLUSIVE_ID,
    EXPORT_BY_IDS_BUTTON_ID,
    IMPORT_IN_RANGE_BUTTON_ID,
    NO_DATA_MESSAGE_ID,
    START_DATE_TIME_INCLUSIVE_ID,
    SUMMARY_ID,
    WORKLOGS_TABLE_BODY_ID,
    WORKLOGS_TABLE_ID,
    WORKLOGS_TABLE_ROW_ID,
} from "../types/constants.js";
import { StateHandler, WorklogsTable } from "../types/types.js";

const { getState, setState }: StateHandler<WorklogsTable> = stateHandler(DEFAULT_WORKLOGS_TABLE);

export async function initialize(): Promise<void> {
    try {
        setState({ data: await getAll(), sort: "startInclusive", direction: "asc" });

        renderTable();
        updateSortIcons();
        updateSummary();
        updateButtons();
    } catch (e) {
        showError(e instanceof Error ? e.message : "Unexpected error.");
    }
}

export async function importInRangeButton(): Promise<void> {
    try {
        setLoading(IMPORT_IN_RANGE_BUTTON_ID, true);

        const { startInclusive, endInclusive } = validateDateTimeRange(
            (document.getElementById(START_DATE_TIME_INCLUSIVE_ID) as HTMLInputElement)?.value,
            (document.getElementById(END_DATE_TIME_INCLUSIVE_ID) as HTMLInputElement)?.value,
        );

        const result = await importInRange(startInclusive, endInclusive);

        if (result === 0) {
            showError("No worklogs found for the given date/time range.");
        } else {
            setState({ data: await getAll() });

            renderTable();
            updateSummary();
            updateButtons();

            showSuccess(`Successfully imported ${result} worklog(s).`);
        }
    } catch (e) {
        showError(e instanceof Error ? e.message : "Unexpected error.");
    } finally {
        setLoading(IMPORT_IN_RANGE_BUTTON_ID, false);
    }
}

export async function exportByIdsButton(): Promise<void> {
    try {
        setLoading(EXPORT_BY_IDS_BUTTON_ID, true);

        const ids = getCheckedIds();

        validateNonEmptyList(ids);

        if (!confirm(`Are you sure you want to export ${ids.length} worklog(s)? This action cannot be undone.`)) return;

        const result = await exportByIds(ids);

        setState({ data: await getAll() });

        renderTable();

        showSuccess(`Successfully exported ${result} worklog(s).`);
    } catch (e) {
        showError(e instanceof Error ? e.message : "Unexpected error.");
    } finally {
        setLoading(EXPORT_BY_IDS_BUTTON_ID, false);
    }
}

export async function deleteButton(): Promise<void> {
    try {
        setLoading(DELETE_BUTTON_ID, true);

        const ids = getCheckedIds();

        if (ids.length === 0) {
            await doFlush();
        } else {
            await doDeleteByIds(ids);
        }

        renderTable();
        updateButtons();
        updateSummary();
    } catch (e) {
        showError(e instanceof Error ? e.message : "Unexpected error.");
    } finally {
        setLoading(DELETE_BUTTON_ID, false);
    }
}

async function doFlush(): Promise<void> {
    validateNonEmptyList(getState().data);

    if (!confirm(`Are you sure you want to delete all worklogs? This action cannot be undone.`)) return;

    await flush();

    setState({ data: await getAll() });

    showSuccess(`Successfully deleted all worklogs.`);
}

async function doDeleteByIds(ids: string[]): Promise<void> {
    if (!confirm(`Are you sure you want to delete ${ids.length} worklog(s)? This action cannot be undone.`)) return;

    validateNonEmptyList(ids);

    const result = await deleteByIds(ids);

    setState({ data: await getAll() });

    showSuccess(`Successfully deleted ${result} worklog(s).`);
}

export function printButton(): void {
    window.print();
}

export function renderTable(): void {
    sortTable();

    const state = getState();

    const table = document.getElementById(WORKLOGS_TABLE_ID) as HTMLElement;
    const tableBody = document.getElementById(WORKLOGS_TABLE_BODY_ID) as HTMLTableElement;
    const noDataMessage = document.getElementById(NO_DATA_MESSAGE_ID) as HTMLElement;

    if (!table || !tableBody || !noDataMessage) return;

    if (state.data.length === 0) {
        table.classList.add("hidden");
        noDataMessage.classList.remove("hidden");

        return;
    }

    tableBody.innerHTML = "";

    state.data.forEach((worklog: Worklog) => {
        const template = document.getElementById(WORKLOGS_TABLE_ROW_ID) as HTMLTemplateElement;
        const row = template?.content.cloneNode(true) as DocumentFragment;

        if (!row) return;

        if (worklog.exported) {
            row.querySelector("tr")?.classList.add("isExported");
        }

        safeQueryParent<HTMLInputElement>(row, ".checkbox", (r) => (r.value = worklog.id));
        safeQueryParent<HTMLTableElement>(row, "td[data-cell='issueKey']", (r) => (r.textContent = worklog.issueKey));
        safeQueryParent<HTMLTableElement>(
            row,
            "td[data-cell='description']",
            (r) => (r.textContent = worklog.description),
        );
        safeQueryParent<HTMLTableElement>(
            row,
            "td[data-cell='startInclusive']",
            (r) => (r.textContent = new Date(worklog.startInclusive).toLocaleString()),
        );
        safeQueryParent<HTMLTableElement>(
            row,
            "td[data-cell='endInclusive']",
            (r) => (r.textContent = new Date(worklog.endInclusive).toLocaleString()),
        );
        safeQueryParent<HTMLTableElement>(
            row,
            "td[data-cell='durationSeconds']",
            (r) => (r.textContent = formatDuration(worklog.durationSeconds)),
        );

        tableBody.appendChild(row);
    });

    table.classList.remove("hidden");
    noDataMessage.classList.add("hidden");
}

export function sortTable(): void {
    const tableState = getState();
    const collator = new Intl.Collator("nl");

    const data: Worklog[] = tableState.data.toSorted((a: Worklog, b: Worklog) => {
        const valA = a[tableState.sort];
        const valB = b[tableState.sort];

        if (typeof valA === "number" && typeof valB === "number") {
            return tableState.direction === "asc" ? valA - valB : valB - valA;
        }

        if (tableState.sort === "startInclusive" || tableState.sort === "endInclusive") {
            const dateA = new Date(valA as string).getTime();
            const dateB = new Date(valB as string).getTime();

            return tableState.direction === "asc" ? dateA - dateB : dateB - dateA;
        }

        const strA = valA.toString().trim().toLowerCase();
        const strB = valB.toString().trim().toLowerCase();

        return tableState.direction === "asc" ? collator.compare(strA, strB) : collator.compare(strB, strA);
    });

    setState({ data: data });
}

export function toggleAllCheckboxes(checked: boolean): void {
    document.querySelectorAll<HTMLInputElement>(".checkbox").forEach((cb: HTMLInputElement) => {
        cb.checked = checked;
    });

    updateButtons();
}

function updateImportInRangeButton(): void {
    const button = document.getElementById(IMPORT_IN_RANGE_BUTTON_ID) as HTMLButtonElement;
    const startInclusive = document.getElementById(START_DATE_TIME_INCLUSIVE_ID) as HTMLInputElement;
    const endInclusive = document.getElementById(END_DATE_TIME_INCLUSIVE_ID) as HTMLInputElement;

    if (button)
        button.disabled =
            (startInclusive && startInclusive.value === "") || (endInclusive && endInclusive.value === "");
}

function updateExportByIdsButton(): void {
    const button = document.getElementById(EXPORT_BY_IDS_BUTTON_ID) as HTMLButtonElement;

    if (button) button.disabled = getCheckedIds().length === 0;
}

function updateDeleteButton(): void {
    const button = document.getElementById(DELETE_BUTTON_ID) as HTMLButtonElement;
    const span = button?.querySelector<HTMLElement>("span");
    const ids = getCheckedIds();

    if (button) button.disabled = getState().data.length === 0;

    if (!span) return;

    if (ids.length === 0) {
        span.textContent = "Flush";
    } else {
        span.textContent = `Delete (${ids.length})`;
    }
}

function updateSummary(): void {
    const state = getState();
    const summary = document.getElementById(SUMMARY_ID) as HTMLElement;

    if (!summary) return;

    if (state.data.length === 0) {
        summary.classList.add("hidden");
        return;
    }

    const totalWorklogs = state.data.length.toString();
    const totalDays = new Set(state.data.map((w) => new Date(w.startInclusive).toLocaleDateString())).size.toString();
    const totalTime = formatDuration(state.data.reduce((acc, w) => acc + w.durationSeconds, 0));

    summary.classList.remove("hidden");
    safeQueryParent<HTMLElement>(
        summary,
        "span[data-summary='total-worklogs']",
        (e) => (e.textContent = totalWorklogs),
    );
    safeQueryParent<HTMLElement>(summary, "span[data-summary='total-days']", (e) => (e.textContent = totalDays));
    safeQueryParent<HTMLElement>(summary, "span[data-summary='total-time']", (e) => (e.textContent = totalTime));
}

function updateSortIcons(): void {
    const tableState = getState();
    const sort = document.getElementById(tableState.sort) as HTMLElement;
    const icon = sort?.querySelector<HTMLElement>(".sort-icon");
    const type = tableState.direction === "asc" ? "arrow-down-0-1" : "arrow-down-1-0";

    document.querySelectorAll(".sort-icon").forEach((icon) => {
        icon.classList.add("hidden");
    });

    icon?.classList.remove("hidden");
    icon?.setAttribute("data-lucide", type);

    generateIcons();
}

export function updateSortKey(key: keyof Worklog): void {
    const tableState = getState();
    const direction = tableState.direction === "asc" ? "desc" : "asc";

    setState({ sort: key, direction: tableState.sort === key ? direction : "asc" });

    updateSortIcons();
}

export function updateButtons(): void {
    updateImportInRangeButton();
    updateExportByIdsButton();
    updateDeleteButton();
}
