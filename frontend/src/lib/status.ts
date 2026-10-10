import type { StatusLevel } from "@/components/rmos/StatusChip";
import type {
    AssetCondition,
    AvailabilityStatus,
    BlockStatus,
    ConflictStatus,
    DepartmentRequest,
    OperationalEvent,
    PriorityLevel,
    ReportStatus,
    TaskStatus,
    Train
} from "@/types/models";

export const priorityChip = (priority: PriorityLevel): StatusLevel =>
    priority === "critical"
        ? "Critical"
        : priority === "high"
          ? "High"
          : priority === "medium"
            ? "Medium"
            : "Low";

export const taskStatusChip = (status: TaskStatus): StatusLevel =>
    status === "pending"
        ? "Pending"
        : status === "scheduled"
          ? "Scheduled"
          : status === "in-progress"
            ? "In Progress"
            : "Completed";

export const requestStatusChip = (status: DepartmentRequest["status"]): StatusLevel =>
    status === "proposed"
        ? "Proposed"
        : status === "approved"
          ? "Approved"
          : status === "modified"
            ? "Modified"
            : "Rejected";

export const conflictStatusChip = (status: ConflictStatus): StatusLevel =>
    status === "open" ? "Open" : status === "acknowledged" ? "Acknowledged" : "Resolved";

export const trainStatusChip = (status: Train["status"]): StatusLevel =>
    status === "on-time"
        ? "On Time"
        : status === "delayed"
          ? "Delayed"
          : status === "diverted"
            ? "Diverted"
            : status === "terminated"
              ? "Terminated"
              : "Scheduled";

export const availabilityChip = (status: AvailabilityStatus): StatusLevel =>
    status === "available"
        ? "Available"
        : status === "maintenance"
          ? "Maintenance"
          : status === "blocked"
            ? "Blocked"
            : "Restricted";

export const blockStatusChip = (status: BlockStatus): StatusLevel =>
    status === "proposed"
        ? "Proposed"
        : status === "approved"
          ? "Approved"
          : status === "rejected"
            ? "Rejected"
            : status === "utilized"
              ? "Utilized"
              : "Released";

export const assetConditionChip = (condition: AssetCondition): StatusLevel =>
    condition === "good"
        ? "Good"
        : condition === "fair"
          ? "Fair"
          : condition === "poor"
            ? "Poor"
            : "Critical";

export const reportStatusChip = (status: ReportStatus): StatusLevel =>
    status === "ready"
        ? "Ready"
        : status === "generating"
          ? "Generating"
          : status === "scheduled"
            ? "Scheduled"
            : "Failed";

export const eventSeverityLabel = (severity: OperationalEvent["severity"]): StatusLevel =>
    severity === "critical" ? "Critical" : severity === "warning" ? "High" : "Medium";
