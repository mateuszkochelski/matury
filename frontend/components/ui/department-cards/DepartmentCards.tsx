"use client";

type Department = {
  id: number;
  name: string;
  url: string;
  university: {
    id: number;
    name: string;
    acronym: string;
    city: string;
  };
};

export default function DepartmentCards({ departments = [] }: { departments?: Department[] }) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
      {departments.map((department) => (
        <div className="bg-blue-200 hover:bg-blue-300 rounded" key={department.id}>
          <a className="w-full h-full flex items-center p-4" href={`/wydzial/${department.id}`}>
            {department.name}
          </a>
        </div>
      ))}
    </div>
  );
}
