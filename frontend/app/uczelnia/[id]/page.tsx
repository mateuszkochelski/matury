import { BACKEND_URL } from "../../constants";
import { Button } from "@/components/ui/button";
import { CustomBreadcrumb } from "@/components/ui/custom-breadcrumb/CustomBradcrumb";
import DepartmentCards from "@/components/ui/department-cards/DepartmentCards";
import UniversityData from "@/components/ui/university-data/UniversityData";
import Link from "next/link";
import { notFound } from "next/navigation";

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

type University = {
  id: number;
  name: string;
  city: string;
  acronym: string;
  url: string;
  description: string;
  address: string;
  longitude: number;
  latitude: number;
};

type DepartmentData = {
  content: Department[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
};

export default async function Home({ params }: { params: { id: string } }) {
  const { id } = await params;
  if (!id) {
    notFound();
  }

  const univeristyResponse = await fetch(`${BACKEND_URL}/api/university/${id}`);
  const universityData: University = await univeristyResponse.json();

  const departmentResponse = await fetch(
    `${BACKEND_URL}/api/department/university/${universityData.id}?size=2137`,
  );
  const departmentData: DepartmentData = await departmentResponse.json();
  const { content: departments } = departmentData;

  return (
    <div className="min-h-screen p-8 pb-20 gap-16 sm:p-20 md:px-40 lg:px-70 font-[family-name:var(--font-geist-sans)]">
      <CustomBreadcrumb
        items={[{ name: "Strona główna", href: "/" }, { name: universityData.name }]}
        className="mb-3"
      />
      <h1 className="mb-5">{universityData.name}</h1>
      <div className="w-full min-h-96 border border-black mb-5">Mapa</div>
      <h2 className="mb-3">Dane podstawowe</h2>
      <UniversityData
        className="mb-5"
        data={[
          { name: "Akronim", value: universityData.acronym },
          { name: "Liczba wydziałów", value: departments.length.toString() },
          { name: "Adres", value: universityData.address },
        ]}
      />
      {universityData.url && universityData.url != "" ? (
        <Link
          className="mb-5 block"
          href={universityData.url}
          target="_blank"
          rel="noopener noreferrer"
        >
          <Button className="bg-blue-200 hover:bg-blue-300 text-black font-normal w-full p-5 border border-black">
            Strona uczelni
          </Button>
        </Link>
      ) : null}
      <h3 className="mb-3">O uczelni</h3>
      <p className="mb-5">{universityData.description}</p>
      <h3 className="mb-3">Wydziały na tej uczelni</h3>
      <DepartmentCards departments={departments}></DepartmentCards>
    </div>
  );
}
