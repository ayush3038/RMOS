import { DATA_MODE } from "@/config/dataMode";
import type { DataRepository } from "./repository";
import { DemoDataRepository } from "./demoRepository";

let _repository: DataRepository | null = null;

export const getRepository = (): DataRepository => {
    if (!_repository) {
        if (DATA_MODE === "SIMULATION") {
            _repository = new DemoDataRepository();
        } else {
            // Future REST implementation injection here
            console.warn("INTEGRATED mode not yet fully implemented. Falling back to DEMO data.");
            _repository = new DemoDataRepository();
        }
    }
    return _repository;
};
