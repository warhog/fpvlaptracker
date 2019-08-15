export interface Alert {
    type: string;
    title: string;
    message: string;
    cssClass?: string;
    permanent?: boolean;
    timeout?: number;
}
