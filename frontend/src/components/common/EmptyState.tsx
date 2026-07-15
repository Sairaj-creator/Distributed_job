import { FileQuestion } from "lucide-react";
import { cn } from "@/utils/cn";

interface EmptyStateProps {
  title: string;
  description: string;
  icon?: React.ReactNode;
  className?: string;
}

export function EmptyState({ title, description, icon, className }: EmptyStateProps) {
  return (
    <div className={cn("flex flex-col items-center justify-center py-16 px-4 text-center", className)}>
      <div className="bg-raised p-4 rounded-full mb-4 text-zinc-400">
        {icon || <FileQuestion size={32} />}
      </div>
      <h3 className="text-lg font-medium text-zinc-200 mb-1">{title}</h3>
      <p className="text-sm text-zinc-400 max-w-sm">{description}</p>
    </div>
  );
}
