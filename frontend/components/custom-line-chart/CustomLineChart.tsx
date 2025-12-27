"use client";

import { useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

type BaseGraphData = {
  year: number;
  [key: string]: number | null;
};

export type ThresholdGraphData = {
  year: number;
  threshold: number | null;
  admissionRate: number | null;
};

export type IncomeGraphData = {
  year: number;
  income: number;
};

type LineChartProps<T extends BaseGraphData> = {
  data: T[][];
  xDataKey: keyof T;
  yDataKey: keyof T;
  showPhases?: boolean;
  tooltipLabel?: string;
};

export default function CustomLineChart<T extends BaseGraphData>({
  data,
  xDataKey,
  yDataKey,
  showPhases = false,
  tooltipLabel = "Wartość",
}: LineChartProps<T>) {
  const [selectedPhase, setSelectedPhase] = useState(0);

  const filteredData = data
    .map((phase) => phase.filter((record) => record[yDataKey] !== null))
    .filter((phase) => phase.length > 0);

  return (
    <>
      <div className="h-64">
        {filteredData.length > 0 && filteredData[selectedPhase]?.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={filteredData[selectedPhase]}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey={xDataKey as string} />
              <YAxis />
              <Tooltip formatter={(value: number) => [value.toLocaleString(), tooltipLabel]} />
              <Line
                type="monotone"
                dataKey={yDataKey as string}
                stroke="var(--primary)"
                strokeWidth={3}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="flex items-center justify-center h-full">Brak danych</div>
        )}
      </div>

      {filteredData.length > 1 && showPhases ? (
        <div className="flex space-x-4 p-4 items-center">
          <p className="text-lg">Faza rekrutacji:</p>
          {[...Array(data.length).keys()].map((phase) => (
            <label
              key={phase}
              className={`cursor-pointer px-4 py-2 rounded-2xl border transition w-15 text-center
          ${
            phase === selectedPhase
              ? "bg-primary text-white border-primary shadow-md"
              : "bg-white text-gray-700 border-gray-300 hover:bg-gray-100"
          }`}
            >
              <input
                type="radio"
                name="phase"
                value={phase}
                checked={selectedPhase === phase}
                onChange={() => setSelectedPhase(phase)}
                className="hidden"
              />
              {phase + 1}
            </label>
          ))}
        </div>
      ) : (
        <></>
      )}
    </>
  );
}
