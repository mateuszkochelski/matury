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

function getAttr<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

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
  tooltipLabel = "Warość",
}: {
  data: ThresholdGraphData[][];
  xDataKey: keyof ThresholdGraphData;
  yDataKey: keyof ThresholdGraphData;
  showPhases: boolean;
  tooltipLabel: string;
}) {
  const [selectedPhase, setSelectedPhase] = useState(0);
  const phases: number[] = [];
  for (let i = 0; i < data.length; i++) {
    phases.push(i);
  }
  let newData = [...data];
  newData.forEach((dataInPhase, phaseIndex) => {
    newData[phaseIndex] = dataInPhase.filter(
      (dataRecord) => getAttr(dataRecord, yDataKey) !== null,
    );
  });
  newData = newData.filter((dataInPhase) => dataInPhase.length > 0);

  return (
    <>
      <div className="h-64">
        {newData.length > 0 && newData[selectedPhase].length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={newData[selectedPhase]}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e0e7ff" />
              <XAxis dataKey={xDataKey} stroke="#64748b" />
              <YAxis stroke="#64748b" />
              <Tooltip
                formatter={(value) => [`${value.toLocaleString()}`, tooltipLabel]}
                labelStyle={{ color: "#2E3C48" }}
                contentStyle={{ backgroundColor: "white", border: "1px solid #90CAF9" }}
              />
              <Line
                type="monotone"
                dataKey={yDataKey}
                stroke="#90CAF9"
                strokeWidth={3}
                dot={{ fill: "#90CAF9", strokeWidth: 2, r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <div className="w-full h-full flex items-center justify-center text-muted-foreground text-lg">
            Nie posiadamy tych danych
          </div>
        )}
      </div>
      {newData.length > 1 && showPhases ? (
        <div className="flex space-x-4 p-4 items-center">
          <p className="text-lg">Faza rekrutacji:</p>
          {phases.map((phase) => (
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
