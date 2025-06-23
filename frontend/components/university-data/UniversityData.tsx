type Data = {
  name: string;
  value: string;
};

export default function UniversityData({
  className = "",
  data = [],
}: {
  className?: string;
  data?: Data[];
}) {
  return (
    <div className={`grid grid-cols-1 sm:grid-cols-2 gap-4 ${className}`}>
      {data.map((e, i) => (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4" key={i}>
          <div className="font-bold col-span-1">{e.name}</div>
          {e.value ? (
            <div className="col-span-2">{e.value}</div>
          ) : (
            <div className="text-gray-400 col-span-2">Brak danych</div>
          )}
        </div>
      ))}
    </div>
  );
}
