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

export type ThresholdGraphData = {
  year: number;
  threshold: number | null;
  admissionRate: number | null;
};

export default function CustomLineChart({
  data,
  xDataKey,
  yDataKey,
  showPhases = false,
  tooltipLabel = "Wartość",
}: {
  data: ThresholdGraphData[][];
  xDataKey: keyof ThresholdGraphData;
  yDataKey: keyof ThresholdGraphData;
  showPhases: boolean;
  tooltipLabel: string;
}) {
  const [selectedPhase, setSelectedPhase] = useState(0);
  const filteredData = data
    .map((dataInPhase) => dataInPhase.filter((dataRecord) => dataRecord[yDataKey] !== null))
    .filter((dataInPhase) => dataInPhase.length > 0);

  return (
    <>
      <div className="h-64">
        {filteredData.length > 0 && filteredData[selectedPhase].length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={filteredData[selectedPhase]}
              margin={{ top: 0, right: 10, left: -15, bottom: 0 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="var(--secondary)" />
              <XAxis dataKey={xDataKey} stroke="var(--foreground)" />
              <YAxis stroke="var(--foreground)" />
              <Tooltip
                formatter={(value) => [`${value.toLocaleString()}`, tooltipLabel]}
                labelStyle={{ color: "var(--foreground)" }}
                contentStyle={{ backgroundColor: "white", border: "1px solid var(--primary)" }}
              />
              <Line
                type="monotone"
                dataKey={yDataKey}
                stroke="var(--primary)"
                strokeWidth={3}
                dot={{ fill: "var(--primary)", strokeWidth: 2, r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="w-full h-full flex items-center justify-center text-muted-foreground text-lg">
            Nie posiadamy tych danych
          </div>
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
