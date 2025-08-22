import {
    ArrowDown01,
    ArrowDown10,
    CalendarDays,
    Clock,
    createIcons,
    Download,
    LoaderCircle,
    Logs,
    NotepadText,
    Printer,
    Share,
    Trash2,
} from "lucide";
import { MessageType, StateHandler, TimeoutId } from "./types/types.js";
import { MESSAGE_BOX_ID } from "./constants/constants";
import { stateHandler } from "./service/stateHandler";

const { getState, setState }: StateHandler<TimeoutId> = stateHandler({ timeoutId: -1 });

export function showMessage(message: string, type: MessageType): void {
    clearTimeout(getState().timeoutId);

    const box = document.getElementById(MESSAGE_BOX_ID) as HTMLElement;

    if (box) {
        box.textContent = message;
        box.classList.add(type);
        box.classList.remove("hidden");

        setState({
            timeoutId: setTimeout(() => {
                box.textContent = "";
                box.classList.add("hidden");
                box.classList.remove("error", "success");
            }, 5000),
        });
    }
}

export function showError(message: string): void {
    showMessage(message, "error");
}

export function showSuccess(message: string): void {
    showMessage(message, "success");
}

export function setLoading(buttonId: string, isLoading: boolean): void {
    const button = document.getElementById(buttonId) as HTMLButtonElement;

    if (button) {
        button.disabled = isLoading;

        if (isLoading) {
            button?.querySelector("[data-lucide]")?.classList.add("hidden");
            button?.prepend((document.getElementById("loader") as HTMLTemplateElement)?.content.cloneNode(true));
        } else {
            button?.querySelector(".loader")?.remove();
            button?.querySelector("[data-lucide]")?.classList.remove("hidden");
        }

        generateIcons();
    }
}

export function formatDuration(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;

    return `${h}h ${m.toString().padStart(2, "0")}m ${s.toString().padStart(2, "0")}s`;
}

export function generateIcons(): void {
    createIcons({
        icons: {
            Clock,
            Download,
            Share,
            Trash2,
            Printer,
            Logs,
            ArrowDown01,
            ArrowDown10,
            LoaderCircle,
            NotepadText,
            CalendarDays,
        },
    });
}

export function validateDateTimeRange(
    startInclusive: string,
    endInclusive: string,
): { startInclusive: string; endInclusive: string } {
    if (new Date(startInclusive) < new Date(endInclusive)) {
        return { startInclusive, endInclusive };
    }

    throw new Error("StartInclusive must be before endInclusive.");
}

export function validateNonEmptyList<T>(ls: T[]): void {
    if (ls.length === 0) {
        throw new Error("Select at least one worklog.");
    }
}

export function getCheckedIds(): string[] {
    return [...document.querySelectorAll<HTMLInputElement>(".checkbox:checked")].map((cb) => cb.value);
}

export function safeQueryParent<T extends Element>(parent: ParentNode, query: string, fn: (t: T) => void): void {
    const elem = parent.querySelector<T>(query);

    if (!elem) return;

    return fn(elem);
}
