import { getUniversityData } from "@/app/utils/getUniversityData";
import { CustomBreadcrumb } from "@/components/custom-breadcrumb/CustomBradcrumb";
import DepartmentCards from "@/components/department-cards/DepartmentCards";
import Map from "@/components/google-map/GoogleMap";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { notFound } from "next/navigation";

export default async function Page({ params }: { params: Promise<{ universityId: string }> }) {
  const { universityId } = await params;
  if (!universityId) {
    notFound();
  }

  const { universityData, departmentData } = await getUniversityData(universityId);
  const departments = departmentData.content;

  return (
    <>
      <CustomBreadcrumb
        items={[{ name: "Strona główna", href: "/" }, { name: universityData.name }]}
      />
      <h1>{universityData.name}</h1>
      <Map query={universityData.name}></Map>
      {universityData.url && universityData.url != "" ? (
        <Link href={universityData.url}>
          <Button className="bg-blue-200 hover:bg-blue-300 text-black font-normal w-full p-5 border border-black cursor-pointer">
            Strona uczelni
          </Button>
        </Link>
      ) : null}
      <h2>O uczelni</h2>
      <p>{universityData.description}</p>
      <h2>Wydziały na tej uczelni</h2>
      <DepartmentCards departments={departments}></DepartmentCards>
    </>
  );
}
